package com.apteka.portal.services;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.servicesecurity.AptekaSecurityService;
import com.apteka.portal.components.validators.LoginValidator;
import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.components.validators.SortingValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;
import com.apteka.portal.dtos.request.apteka.AdressRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaRequestDTO;
import com.apteka.portal.dtos.request.apteka.AptekaUpdateDescriptionRequestDTO;
import com.apteka.portal.dtos.response.apteka.AptekaResponseDTO;
import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.DuplicateAptekaFullNameException;
import com.apteka.portal.exceptions.DuplicateAptekaLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidAptekaNumberException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Address;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AptekaService {
    private final AptekaRepository aptekaRepository;
    private final UserGroupRepository userGroupRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;
    private final AuthService authService;
    private final LoginValidator loginValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final AptekaSecurityService aptekaSecurityService;
    private final AccountRepository accountRepository;
    private final SseController sseController;
    private final SortingValidator sortingValidator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "number",
            "address.city",
            "address.street",
            "account.userGroup.id",
            "createdAt",
            "updatedAt",
            "account.login",
            "account.phoneNumber");

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("account.userGroup.id"),
            Sort.Order.asc("number"));

    @Transactional(readOnly = true)
    public Page<AptekaResponseDTO> getAll(Pageable pageable, boolean isActive, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanSeeSaveDeleted(currentUser, isActive);
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Integer scopeGroupId = aptekaSecurityService.canSelectAllAptekas(currentUser)
                ? null : currentUser.getUserGroup().getId();
        return aptekaRepository.findAll(isActive, scopeGroupId, validatedPageable).map(AptekaResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public AptekaResponseDTO getOne(UUID id, boolean isActive, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanSeeSaveDeleted(currentUser, isActive);
        Apteka apteka = aptekaRepository.findByIdWithAccount(id, isActive)
                .orElseThrow(() -> new AptekaNotFoundException(id));
        aptekaSecurityService.validateCanSelectApteka(currentUser, apteka.getAccount().getUserGroup().getId());
        return AptekaResponseDTO.from(apteka);
    }

    @Transactional(readOnly = true)
    public Page<AptekaResponseDTO> filter(AptekaFilterRequestDTO dto, Pageable pageable, boolean isActive,
            AppUserDetails currentUser) {
        aptekaSecurityService.validateCanSeeSaveDeleted(currentUser, isActive);
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Integer scopeGroupId = aptekaSecurityService.canSelectAllAptekas(currentUser)
                ? dto.groupId() : currentUser.getUserGroup().getId();
        return aptekaRepository.filter(dto.login(),
                scopeGroupId,
                dto.number(),
                dto.phoneNumber(),
                isActive,
                dto.city(),
                dto.street(),
                validatedPageable).map(AptekaResponseDTO::from);
    }

    @Transactional
    public AptekaResponseDTO create(AptekaRequestDTO dto, AppUserDetails currentUser) {

        aptekaSecurityService.validateCanCreateApteka(currentUser);

        String cleanLogin = loginValidator.getCleanLogin(dto.login());
        passwordValidator.validatePassword(dto.password(), false);
        UserGroup userGroup = userGroupRepository.findById(dto.groupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.groupId()));
        validateAptekaNumberInGroup(dto.number(), dto.groupId());
        String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());

        Address address = Address.builder()
                .city(dto.adressRequestDTO().city())
                .street(dto.adressRequestDTO().street())
                .house(dto.adressRequestDTO().house())
                .fiasId(dto.adressRequestDTO().fiasId())
                .build();

        Apteka apteka = Apteka.builder()
                .number(dto.number())
                .address(address)
                .createdBy(currentUser.getDisplayName())
                .build();

        Account account = Account.builder()
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(cleanPhoneNumber)
                .userGroup(userGroup)
                .apteka(apteka)
                .isActive(true)
                .build();

        apteka.setAptekaName(userGroup.getName() + " " + apteka.getNumber());
        apteka.setAccount(account);
        aptekaRepository.save(apteka);
        Integer userGroupId = account.getUserGroup().getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                }
            });
        }

        return AptekaResponseDTO.from(apteka);
    }

    @Transactional
    public AptekaResponseDTO update(UUID id, AptekaUpdateRequestDTO dto, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanUpdateAccountApteka(currentUser);
        aptekaSecurityService.validateCanUpdateDescriptionApteka(currentUser);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));
        Apteka apteka = account.getApteka();

        UpdateAptekaAccountResponseDTO accountResponseDTO = updateAptekaAccount(account,
                AccountUpdateRequestDTO.fromAptekaFullRequest(dto), currentUser);
        UpdateAptekaDescriptionResponseDTO aptekaResponseDTO = updateAptekaDescription(apteka,
                AptekaUpdateDescriptionRequestDTO.from(dto), currentUser);

        boolean hasChange = accountResponseDTO.hasChange() || aptekaResponseDTO.hasChange();
        boolean needsLogout = accountResponseDTO.needsLogout();

        String oldLogin = accountResponseDTO.oldLogin();

        if (hasChange) {
            UUID aptekaId = apteka.getId();
            apteka.setUpdatedAt(Instant.now());
            apteka.setUpdatedBy(currentUser.getDisplayName());
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (needsLogout) {
                            authService.invalidateAllSession(oldLogin);
                        }
                        var signal = new SseEventNames.EntityUpdateSignalDTO(aptekaId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                    }
                });
            }
        }

        return AptekaResponseDTO.from(apteka);
    }

    @Transactional
    public AptekaResponseDTO updateAccount(UUID id, AccountUpdateRequestDTO dto, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanUpdateAccountApteka(currentUser);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));

        UpdateAptekaAccountResponseDTO accountResponseDTO = updateAptekaAccount(account, dto, currentUser);

        boolean hasChange = accountResponseDTO.hasChange();
        boolean needsLogout = accountResponseDTO.needsLogout();

        String oldLogin = accountResponseDTO.oldLogin();

        Apteka apteka = account.getApteka();

        if (hasChange) {
            UUID aptekaId = account.getId();
            apteka.setUpdatedAt(Instant.now());
            apteka.setUpdatedBy(currentUser.getDisplayName());
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (needsLogout) {
                            authService.invalidateAllSession(oldLogin);
                        }
                        var signal = new SseEventNames.EntityUpdateSignalDTO(aptekaId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                    }
                });
            }
        }
        return AptekaResponseDTO.from(apteka);
    }

    @Transactional
    public AptekaResponseDTO updateDescription(UUID id, AptekaUpdateDescriptionRequestDTO dto,
            AppUserDetails currentUser) {
        aptekaSecurityService.validateCanUpdateDescriptionApteka(currentUser);

        Apteka apteka = aptekaRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));

        UpdateAptekaDescriptionResponseDTO aptekaResponseDTO = updateAptekaDescription(apteka,
                dto, currentUser);

        boolean hasChange = aptekaResponseDTO.hasChange();

        if (hasChange) {
            UUID aptekaId = apteka.getId();
            apteka.setUpdatedAt(Instant.now());
            apteka.setUpdatedBy(currentUser.getDisplayName());
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(aptekaId, SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                    }
                });
            }
        }

        return AptekaResponseDTO.from(apteka);
    }

    @Transactional
    public void safeDelete(UUID id, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanSafeDeleteApteka(currentUser);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));

        UUID aptekaId = account.getId();
        account.setActive(false);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.EntityUpdateSignalDTO(aptekaId,
                            SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                }
            });
        }
    }

    @Transactional
    public void permanentDelete(UUID id, AppUserDetails currentUser) {
        aptekaSecurityService.validateCanPermanentDeleteApteka(currentUser);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));
        Integer userGroupId = account.getUserGroup().getId();
        accountRepository.delete(account);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
                }
            });
        }
    }

    private final record UpdateAptekaAccountResponseDTO(boolean hasChange, boolean needsLogout,
            String oldLogin) {
    }

    private UpdateAptekaAccountResponseDTO updateAptekaAccount(Account account, AccountUpdateRequestDTO dto,
            AppUserDetails currentUser) {
        aptekaSecurityService.validateCanUpdateAccountApteka(currentUser);
        Apteka apteka = account.getApteka();

        boolean hasChange = false;
        boolean needsLogout = false;
        String oldLogin = account.getLogin();

        if (StringUtils.hasText(dto.login())) {
            String newLogin = loginValidator.getCleanLogin(dto.login());
            if (!Objects.equals(newLogin, oldLogin)) {
                validateLogin(newLogin);
                account.setLogin(newLogin);
                needsLogout = true;
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.password())) {
            passwordValidator.validatePassword(dto.password(), false);
            if (passwordEncoder.matches(dto.password(), account.getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            account.setPassword(passwordEncoder.encode(dto.password()));
            needsLogout = true;
            hasChange = true;
        }

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            if (!Objects.equals(cleanPhoneNumber, apteka.getAccount().getPhoneNumber())) {
                apteka.getAccount().setPhoneNumber(cleanPhoneNumber);
                hasChange = true;
            }
        }

        if (dto.groupId() != null && dto.groupId() > 0) {
            UserGroup userGroup = userGroupRepository.findById(dto.groupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.groupId()));
            if (!Objects.equals(userGroup.getId(), account.getUserGroup().getId())) {
                validateAptekaNumberInGroup(apteka.getNumber(), dto.groupId());
                account.setUserGroup(userGroup);
                hasChange = true;
            }
        }
        return new UpdateAptekaAccountResponseDTO(hasChange, needsLogout, oldLogin);
    }

    private final record UpdateAptekaDescriptionResponseDTO(boolean hasChange) {
    }

    private UpdateAptekaDescriptionResponseDTO updateAptekaDescription(Apteka apteka,
            AptekaUpdateDescriptionRequestDTO dto,
            AppUserDetails currentUser) {
        aptekaSecurityService.validateCanUpdateDescriptionApteka(currentUser);

        Account account = apteka.getAccount();

        boolean hasChange = false;

        AdressRequestDTO adressRequestDTO = dto.adressRequestDTO();
        Address adress = apteka.getAddress();

        if (StringUtils.hasText(adressRequestDTO.city())) {
            if (!Objects.equals(adressRequestDTO.city(), adress.getCity())) {
                adress.setCity(adressRequestDTO.city());
                hasChange = true;
            }
        }
        if (StringUtils.hasText(adressRequestDTO.street())) {
            if (!Objects.equals(adressRequestDTO.street(), adress.getStreet())) {
                adress.setStreet(adressRequestDTO.street());
                hasChange = true;
            }
        }
        if (StringUtils.hasText(adressRequestDTO.house())) {
            if (!Objects.equals(adressRequestDTO.house(), adress.getHouse())) {
                adress.setHouse(adressRequestDTO.house());
                hasChange = true;
            }
        }
        if (adressRequestDTO.fiasId() != null) {
            if (!Objects.equals(adressRequestDTO.fiasId(), adress.getFiasId())) {
                adress.setFiasId(adressRequestDTO.fiasId());
            }
        }

        if (dto.number() != null && dto.number() > 0) {
            if (!dto.number().equals(apteka.getNumber())) {
                validateAptekaNumberInGroup(dto.number(), account.getUserGroup().getId());
                apteka.setNumber(dto.number());
                hasChange = true;
            }
        }

        return new UpdateAptekaDescriptionResponseDTO(hasChange);
    }

    private void validateAptekaNumberInGroup(Integer number, Integer groupId) {
        if (number == 0 || number == null) {
            throw new InvalidAptekaNumberException();
        }
        if (aptekaRepository.existsByAccount_UserGroup_IdAndNumber(groupId, number)) {
            throw new DuplicateAptekaFullNameException("Аптека с таким юридическим лицом и номером уже существует");
        }
    }

    private void validateLogin(String login) {
        if (aptekaRepository.existsByAccount_Login(login)) {
            throw new DuplicateAptekaLoginException(login);
        }
    }
}
