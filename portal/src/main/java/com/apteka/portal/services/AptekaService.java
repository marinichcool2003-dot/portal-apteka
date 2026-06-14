package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.validators.AdressValidator;
import com.apteka.portal.components.validators.LoginValidator;
import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.AptekaFilterRequestDTO;
import com.apteka.portal.dtos.request.AptekaRequestDTO;
import com.apteka.portal.dtos.request.AptekaUpdateRequestDTO;
import com.apteka.portal.dtos.response.AptekaResponseDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.DublicateAptekaFullNameException;
import com.apteka.portal.exceptions.DublicateAptekaLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidAptekaNumberException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
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
    private final AdressValidator adressValidator;

    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<AptekaResponseDTO> getAll() {
        return aptekaRepository.findAll().stream()
            .map(AptekaResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public AptekaResponseDTO getOne(Integer id) {
        Apteka apteka = aptekaRepository.findById(id)
                .orElseThrow(() -> new AptekaNotFoundException(id));
        return AptekaResponseDTO.from(apteka);
    }

    @Transactional(readOnly = true)
    public List<AptekaResponseDTO> filter(AptekaFilterRequestDTO dto) {
        return aptekaRepository.filter(dto.login(), dto.groupId(), dto.number(), dto.phoneNumber())
            .stream().map(AptekaResponseDTO::from).toList();
    }

    @Transactional
    public AptekaResponseDTO create(AptekaRequestDTO dto, AppUserDetails currentUser) {
        hasAccessToApteki(currentUser);
        String cleanLogin = loginValidator.getCleanLogin(dto.login());
        passwordValidator.validatePassword(dto.password(), false);
        UserGroup userGroup = userGroupRepository.findById(dto.groupId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.groupId()));
        validateAptekaNumberInGroup(dto.number(), dto.groupId());
        String cleanAdress = adressValidator.getCleanAdress(dto.adress());
        String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());

        Apteka apteka = Apteka.builder()
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .number(dto.number())
                .adress(cleanAdress)
                .userGroup(userGroup)
                .phoneNumber(cleanPhoneNumber)
                .build();

        aptekaRepository.save(apteka);

        var signal = new SseEventNames.AppUserDetailsSignalDTO(apteka.getUserGroup().getId(), SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);

        return AptekaResponseDTO.from(apteka);
    }

    @Transactional
    public AptekaResponseDTO update(Integer id, AptekaUpdateRequestDTO dto, AppUserDetails currentUser) {
        hasAccessToApteki(currentUser);

        Apteka apteka = aptekaRepository.findById(id)
            .orElseThrow(() -> new AptekaNotFoundException(id));
        String oldLogin = apteka.getLogin();
        boolean needsLogout = false;

        if (StringUtils.hasText(dto.login())) {
            String newLogin = loginValidator.getCleanLogin(dto.login());
            if (!Objects.equals(newLogin, oldLogin)) {
                validateLogin(newLogin);
                apteka.setLogin(newLogin);
                needsLogout = true;
            }
        }

        if (StringUtils.hasText(dto.password())) {
            passwordValidator.validatePassword(dto.password(), false);
            if (passwordEncoder.matches(dto.password(), apteka.getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            apteka.setPassword(passwordEncoder.encode(dto.password()));
            needsLogout = true;
        }

        if (StringUtils.hasText(dto.adress())) {
            String cleanAdress = adressValidator.getCleanAdress(dto.adress());
            apteka.setAdress(cleanAdress);
        }

        if (dto.number() != null && dto.number() > 0) {
            if (!dto.number().equals(apteka.getNumber())) {
                validateAptekaNumberInGroup(dto.number(), apteka.getUserGroup().getId());
                apteka.setNumber(dto.number());
            }
        }

        if (dto.groupId() != null && dto.groupId() > 0) {
            UserGroup userGroup = userGroupRepository.findById(dto.groupId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.groupId()));
            validateAptekaNumberInGroup(apteka.getNumber(), dto.groupId());
            apteka.setUserGroup(userGroup);
        }

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            apteka.setPhoneNumber(cleanPhoneNumber);
        }

        Apteka savedApteka = aptekaRepository.save(apteka);

        if (needsLogout) {
            authService.invalidateAllSession(oldLogin);
        }

        var signal = new SseEventNames.EntityUpdateSignalDTO(apteka.getId(), SseSignalTypes.UPDATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);

        return AptekaResponseDTO.from(savedApteka);
    }

    @Transactional
    public void delete(Integer id, AppUserDetails currentUser) {
        hasAccessToApteki(currentUser);
        Apteka apteka = aptekaRepository.findById(id)
            .orElseThrow(() -> new AptekaNotFoundException(id));
        aptekaRepository.delete(apteka);

        var signal = new SseEventNames.AppUserDetailsSignalDTO(apteka.getUserGroup().getId(), SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_APTEKI, signal);
    }

    private void validateAptekaNumberInGroup(Integer number, Integer groupId) {
        if (number == 0 || number == null) {
            throw new InvalidAptekaNumberException();
        }
        if (aptekaRepository.existsByUserGroup_IdAndNumber(groupId, number)) {
            throw new DublicateAptekaFullNameException("Аптека с таким юридическим лицом и номером уже существует");
        }
    }

    private void validateLogin(String login) {
        if (aptekaRepository.existsByLogin(login)) {
            throw new DublicateAptekaLoginException(login);
        }
    }

    private void hasAccessToApteki(AppUserDetails currentUser) {
        if (!currentUser.hasAnyRole(UserRole.ADMIN, UserRole.BOSS)) {
            throw new AccessDeniedException("Только администратор или начальник может добавлять аптеки");
        }
    }
}
