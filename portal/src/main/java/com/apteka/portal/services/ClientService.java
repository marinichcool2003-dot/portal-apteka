package com.apteka.portal.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.components.AvatarService;
import com.apteka.portal.components.servicesecurity.ClientSecurityService;
import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientCreateRequestDTO;
import com.apteka.portal.dtos.request.client.ClientFilterRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateFullRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdatePersonalProfileRequestDTO;
import com.apteka.portal.dtos.request.client.ClientUpdateDescriptionRequestDTO;
import com.apteka.portal.components.validators.FullNameValidator;
import com.apteka.portal.components.validators.LoginValidator;
import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.response.AssignedStatsDTO;
import com.apteka.portal.dtos.response.CreatedStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.dtos.response.client.ClientResponseDTO;
import com.apteka.portal.dtos.response.client.ClientWithStatsDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.UserHaveActiveTasksException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final AuthService authService;
    private final ClientRepository clientRepository;
    private final AvatarService avatarClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupRepository userGroupRepository;
    private final TaskRepository taskRepository;
    private final ClientSecurityService clientSecurityService;
    private final PasswordValidator passwordValidator;
    private final LoginValidator loginValidator;
    private final FullNameValidator fullNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;
    private final AccountRepository accountRepository;

    private final SseController sseController;

    @Value("${app.default.avatars.upload.dir}")
    private String uploadAvatarDir;

    @Value("${app.default.avatars.upload.picture.user}")
    private String uploadAvatarPictureName;

    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getAll(AppUserDetails currentUser, Pageable pageable, Boolean isActive) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        if (Boolean.FALSE.equals(isActive)) {
            clientSecurityService.validateWhoCanSelectNonActiveClients(currentUser, null);
        }

        return clientRepository.findAll(pageable, isActive)
                .map(ClientResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getOne(UUID id, AppUserDetails currentUser) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        Account account = client.getAccount();
        Boolean isActive = account.isActive();
        UserGroup userGroup = account.getUserGroup();
        if (!isActive) {
            clientSecurityService.validateWhoCanSelectNonActiveClients(currentUser, userGroup);
        }
        return ClientResponseDTO.from(client);
    }

    @Transactional(readOnly = true)
    public TaskStatsDTO getMyStats(AppUserDetails currentUser) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        List<AssignedStatsDTO> assignedStatsList = taskRepository
                .getClientAssignedStatsBatch(List.of(currentUser.getInternalId()));
        List<CreatedStatsDTO> createdStatsList = taskRepository
                .getClientCreatedStatsBatch(List.of(currentUser.getInternalId()));

        AssignedStatsDTO assignedStats = assignedStatsList.isEmpty()
                ? new AssignedStatsDTO(currentUser.getInternalId(), 0L, 0L, 0L, 0L, 0L)
                : assignedStatsList.getFirst();

        CreatedStatsDTO createdStats = createdStatsList.isEmpty()
                ? new CreatedStatsDTO(currentUser.getInternalId(), 0L)
                : createdStatsList.getFirst();

        return new TaskStatsDTO(assignedStats, createdStats);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponseDTO> getByGroup(Integer userGroupId, AppUserDetails currentUser, Pageable pageable,
            Boolean isActive) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);

        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new GroupUserNotFoundException(userGroupId));

        if (Boolean.FALSE.equals(isActive)) {
            clientSecurityService.validateWhoCanSelectNonActiveClients(currentUser, userGroup);
        }
        return clientRepository.findByUserGroupId(userGroupId, isActive, pageable).map(ClientResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public List<ClientWithStatsDTO> getWithNumberOfTask(Integer userGroupId, AppUserDetails currentUser,
            Boolean isActive) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);

        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new GroupUserNotFoundException(userGroupId));

        clientSecurityService.validateWhoCanSelectClientStats(currentUser, userGroup);

        if (Boolean.FALSE.equals(isActive)) {
            clientSecurityService.validateWhoCanSelectNonActiveClients(currentUser, userGroup);
        }

        List<ClientResponseDTO> clients = clientRepository.findByUserGroupId(userGroupId, isActive).stream()
                .map(ClientResponseDTO::from).toList();
        if (clients.isEmpty())
            return List.of();

        List<UUID> clientIds = clients.stream().map(ClientResponseDTO::id).toList();

        Map<UUID, AssignedStatsDTO> statsMap = taskRepository.getClientAssignedStatsBatch(clientIds)
                .stream()
                .collect(Collectors.toMap(AssignedStatsDTO::clientId, dto -> dto));

        return clients.stream()
                .map(clientDto -> new ClientWithStatsDTO(
                        clientDto,
                        statsMap.getOrDefault(clientDto.id(),
                                new AssignedStatsDTO(clientDto.id(), 0L, 0L, 0L, 0L, 0L))))
                .toList();
    }

    @Transactional
    public Page<ClientResponseDTO> filter(ClientFilterRequestDTO dto, AppUserDetails currentUser, Pageable pageable) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        return clientRepository.filter(
                pageable,
                dto.login(),
                dto.phoneNumber(),
                dto.groupId(),
                true,
                dto.fullName(),
                dto.extensionNumber())
                .map(ClientResponseDTO::from);
    }

    @Transactional
    public ClientResponseDTO create(ClientCreateRequestDTO dto, AppUserDetails currentUser) throws IOException {

        UserGroup userGroup = userGroupRepository.findById(dto.groupClientId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.groupClientId()));

        clientSecurityService.validateCanCreateClient(currentUser, userGroup);

        String cleanLogin = loginValidator.getCleanLogin(dto.login());
        validateLogin(dto.login());
        String normalizedName = fullNameValidator.getCleanFullName(dto.fullName());
        passwordValidator.validatePassword(dto.password(), true);
        String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
        String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());

        UserRole role = UserRole.fromCode(dto.roleCode());
        clientSecurityService.canGiveRole(currentUser, role);

        Set<AccountAction> actions = dto.accountActionsCode().stream()
                .map(AccountAction::fromCode).collect(Collectors.toSet());

        Account.AccountBuilder accountBuilder = Account.builder().userRole(role).userGroup(userGroup).isActive(true);
        clientSecurityService.canAddActions(actions, currentUser, accountBuilder.build());

        Client newClient = Client.builder()
                .fullName(normalizedName)
                .extensionNumber(cleanExtensionNumber)
                .avatarURL(uploadAvatarDir.concat(uploadAvatarPictureName))
                .createdBy(currentUser.getDisplayName())
                .build();

        Account account = accountBuilder
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(cleanPhoneNumber)
                .client(newClient)
                .build();

        newClient.setAccount(account);

        Client client = clientRepository.save(newClient);
        Integer userGroupId = userGroup.getId();

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                }
            });
        }

        return ClientResponseDTO.from(client);
    }

    @Transactional
    public ClientResponseDTO updateAccount(UUID id, AccountUpdateRequestDTO dto, AppUserDetails currentUser) {
        UpdateAccountResponseWithOldLoginDTO responseDTO = updateAccountInner(id, dto, currentUser);
        Account account = responseDTO.accountResponseDTO().account();
        String oldLogin = responseDTO.oldLogin();
        boolean hasChange = responseDTO.accountResponseDTO().hasChange();
        boolean needsLogout = responseDTO.accountResponseDTO().needsLogout();
        boolean onlyForCurrentUser = responseDTO.accountResponseDTO().isOnlyForCurrent();

        Client client = account.getClient();

        if (hasChange || onlyForCurrentUser) {
            client.setUpdatedBy(currentUser.getDisplayName());

            if (needsLogout) {
                authService.invalidateAllSession(oldLogin);
            }
        }

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (hasChange || onlyForCurrentUser) {
            String clientIdString = client.getId().toString();

            final boolean isOnlyForCurrent = onlyForCurrentUser;

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        if (isOnlyForCurrent) {
                            sseController.sendNotification(clientIdString, SseEventNames.REFRESH_CLIENTS, response);
                        } else {
                            var signal = new SseEventNames.EntityUpdateSignalDTO(clientIdString,
                                    SseSignalTypes.UPDATED);
                            sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                        }
                    }
                });
            }
        }

        return response;
    }

    @Transactional
    public ClientResponseDTO updateClientDescription(UUID id, ClientUpdateDescriptionRequestDTO dto,
            AppUserDetails currentUser) {
        UpdateClientResponseDTO responseDTO = updateClientDescriptionInner(id, dto, currentUser);
        Client client = responseDTO.client();
        boolean hasChange = responseDTO.hasChange();

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (hasChange) {
            String clientIdString = client.getId().toString();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        var signal = new SseEventNames.EntityUpdateSignalDTO(clientIdString,
                                SseSignalTypes.UPDATED);
                        sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                    }
                });
            }
        }
        return response;
    }

    @Transactional
    public ClientResponseDTO updatePersonalProfile(ClientUpdatePersonalProfileRequestDTO dto,
            AppUserDetails currentUser) throws IOException {
        ClientInnerResponseDTO responseDTO = updateProfile(currentUser.getInternalId(), dto, currentUser);

        boolean hasChange = responseDTO.hasChange();
        boolean onlyForCurrentUser = responseDTO.isOnlyForCurrent();
        boolean needsLogout = responseDTO.needsLogout();
        Client client = responseDTO.client();
        String oldLogin = responseDTO.oldLogin();

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (hasChange || onlyForCurrentUser) {
            client.setUpdatedBy(currentUser.getDisplayName());

            if (needsLogout) {
                authService.invalidateAllSession(oldLogin);
            }
            UUID clientId = client.getId();
            String clientIdString = clientId.toString();

            final boolean isOnlyForCurrent = onlyForCurrentUser;

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            avatarClientService.deleteClientAvatarIfExists(clientId);
                        } else if (status == STATUS_COMMITTED) {
                            if (isOnlyForCurrent) {
                                sseController.sendNotification(clientIdString, SseEventNames.REFRESH_CLIENTS, response);
                            } else {
                                var signal = new SseEventNames.EntityUpdateSignalDTO(clientIdString,
                                        SseSignalTypes.UPDATED);
                                sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                            }
                        }
                    }
                });
            }
        }

        return response;
    }

    @Transactional
    public ClientResponseDTO updateFullClient(UUID id, ClientUpdateFullRequestDTO dto, AppUserDetails currentUser)
            throws IOException {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        clientSecurityService.validateCanUpdateFullClient(currentUser, account);

        ClientInnerResponseDTO responseDTO = updateProfile(id, new ClientUpdatePersonalProfileRequestDTO(
                dto.accountUpdateRequestDTO(),
                dto.clientUpdateRequestDTO(), dto.avatar()),
                currentUser);

        boolean hasChange = responseDTO.hasChange();
        boolean onlyForCurrentUser = responseDTO.isOnlyForCurrent();
        boolean needsLogout = responseDTO.needsLogout();
        Client client = responseDTO.client();
        account = client.getAccount();
        String oldLogin = responseDTO.oldLogin();

        if (dto.userGroupId() != null) {
            UpdateAccountResponseDTO groupResult = updateUserGroup(account, dto.userGroupId(), hasChange, needsLogout,
                    onlyForCurrentUser);

            hasChange = groupResult.hasChange();
            needsLogout = groupResult.needsLogout();
            onlyForCurrentUser = groupResult.isOnlyForCurrent();

            client.setAccount(groupResult.account());
        }

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (hasChange || onlyForCurrentUser) {
            client.setUpdatedBy(currentUser.getDisplayName());

            if (needsLogout) {
                authService.invalidateAllSession(oldLogin);
            }
            String clientIdString = client.getId().toString();

            final boolean isOnlyForCurrent = onlyForCurrentUser;

            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status == STATUS_ROLLED_BACK) {
                            avatarClientService.deleteClientAvatarIfExists(id);
                        } else if (status == STATUS_COMMITTED) {
                            if (isOnlyForCurrent) {
                                sseController.sendNotification(clientIdString, SseEventNames.REFRESH_CLIENTS, response);
                            } else {
                                var signal = new SseEventNames.EntityUpdateSignalDTO(clientIdString,
                                        SseSignalTypes.UPDATED);
                                sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                            }
                        }
                    }
                });
            }
        }

        return response;
    }

    @Transactional
    public void addActionsToClient(UUID id, Set<String> actionsCode, AppUserDetails currentUser) {
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        Account account = client.getAccount();

        Set<AccountAction> actions = getAllActionsFromCode(actionsCode);

        clientSecurityService.canAddActions(actions, currentUser, account);

        Set<AccountAction> accountActions = account.getActions();
        accountActions.addAll(actions);
        client.setAccount(account);

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.sendNotification(id.toString(), SseEventNames.REFRESH_CLIENTS, response);
                }
            });
        }
    }

    @Transactional
    public void removeActionsClient(UUID id, Set<String> actionsCode, AppUserDetails currentUser) {
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        Account account = client.getAccount();

        Set<AccountAction> actions = getAllActionsFromCode(actionsCode);

        clientSecurityService.canRemoveActions(actions, currentUser, account);

        Set<AccountAction> accountActions = account.getActions();
        accountActions.removeAll(actions);
        client.setAccount(account);

        ClientResponseDTO response = ClientResponseDTO.from(client);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sseController.sendNotification(id.toString(), SseEventNames.REFRESH_CLIENTS, response);
                }
            });
        }
    }

    @Transactional
    public void selfDelete(UUID id, AppUserDetails currentUser) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        clientSecurityService.canSaveDelete(currentUser, account);

        Integer userGroupId = account.getUserGroup().getId();
        account.setActive(false);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                }
            });
        }
    }

    @Transactional
    public void restoreAfterSafeDelete(UUID id, AppUserDetails currentUser) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        clientSecurityService.activateAfterSafeDelete(currentUser, account);

        Integer userGroupId = account.getUserGroup().getId();
        account.setActive(true);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.UPDATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                }
            });
        }
    }

    @Transactional
    public void permanentDelete(UUID id, AppUserDetails currentUser) {
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        Account account = client.getAccount();
        clientSecurityService.canPermanentDelete(currentUser, account);

        Integer userGroupId = account.getUserGroup().getId();
        clientRepository.delete(client);
        avatarClientService.deleteClientAvatarIfExists(id);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var signal = new SseEventNames.AppUserDetailsSignalDTO(userGroupId,
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
                }
            });
        }
    }

    private record ClientInnerResponseDTO(Client client, boolean hasChange, boolean needsLogout,
            boolean isOnlyForCurrent, String oldLogin) {
    }

    private record UpdateAccountResponseWithOldLoginDTO(UpdateAccountResponseDTO accountResponseDTO, String oldLogin) {
    }

    private record UpdateAccountResponseDTO(Account account, boolean hasChange, boolean needsLogout,
            boolean isOnlyForCurrent) {
    }

    private record UpdateClientResponseDTO(Client client, boolean hasChange) {
    }

    private UpdateAccountResponseWithOldLoginDTO updateAccountInner(UUID id, AccountUpdateRequestDTO dto,
            AppUserDetails currentUser) {
        Account account = accountRepository.findByIdWithUserGroup(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        clientSecurityService.validateCanUpdateClientAccount(currentUser, account);

        String oldLogin = account.getLogin();

        boolean hasChange = false;
        boolean needsLogout = false;
        boolean onlyForCurrentUser = false;

        if (StringUtils.hasText(dto.login())) {
            String cleanLogin = loginValidator.getCleanLogin(dto.login());
            if (!Objects.equals(cleanLogin, account.getLogin())) {
                validateLogin(cleanLogin);
                account.setLogin(cleanLogin);
                hasChange = true;
                needsLogout = true;
            }
        }

        if (StringUtils.hasText(dto.phoneNumber())) {
            String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
            if (!Objects.equals(cleanPhoneNumber, account.getPhoneNumber())) {
                account.setPhoneNumber(cleanPhoneNumber);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.password())) {
            passwordValidator.validatePassword(dto.password(), true);
            if (passwordEncoder.matches(dto.password(), account.getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            account.setPassword(passwordEncoder.encode(dto.password()));
            needsLogout = true;
            onlyForCurrentUser = true;
        }

        return new UpdateAccountResponseWithOldLoginDTO(
                new UpdateAccountResponseDTO(account, hasChange, needsLogout, onlyForCurrentUser), oldLogin);
    }

    private ClientInnerResponseDTO updateProfile(UUID id, ClientUpdatePersonalProfileRequestDTO dto,
            AppUserDetails currentUser)
            throws IOException {

        UpdateAccountResponseWithOldLoginDTO accountResponseDTO = updateAccountInner(id,
                dto.accountUpdateRequestDTO(), currentUser);
        UpdateClientResponseDTO clientResponseDTO = updateClientDescriptionInner(id,
                dto.clientUpdateRequestDTO(), currentUser);

        Account account = accountResponseDTO.accountResponseDTO().account();
        String oldLogin = accountResponseDTO.oldLogin();
        boolean hasChange = accountResponseDTO.accountResponseDTO().hasChange() || clientResponseDTO.hasChange();
        boolean needsLogout = accountResponseDTO.accountResponseDTO().needsLogout();
        boolean onlyForCurrentUser = accountResponseDTO.accountResponseDTO().isOnlyForCurrent();

        Client client = clientResponseDTO.client();
        client.setAccount(account);

        if (dto.avatar() != null && !dto.avatar().isEmpty()) {
            updateAvatar(client, dto.avatar());
            hasChange = true;
        }

        return new ClientInnerResponseDTO(client, hasChange, needsLogout, onlyForCurrentUser, oldLogin);
    }

    private UpdateAccountResponseDTO updateUserGroup(Account account, Integer userGroupId, boolean hasChange,
            boolean needsLogout, boolean isOnlyForCurrent) {
        UUID accountId = account.getId();
        boolean haveActiveTasks = taskRepository.existsByClientIdAndStatus(accountId,
                Set.of(TaskStatus.OPEN, TaskStatus.PROCESSED));
        if (haveActiveTasks) {
            throw new UserHaveActiveTasksException(accountId);
        }
        UserGroup userGroup = userGroupRepository.findById(userGroupId)
                .orElseThrow(() -> new GroupUserNotFoundException(userGroupId));
        if (!Objects.equals(account.getUserGroup().getId(), userGroup.getId())) {
            account.setUserGroup(userGroup);
            hasChange = true;
            needsLogout = true;
            isOnlyForCurrent = false;
            return new UpdateAccountResponseDTO(account, hasChange, needsLogout, isOnlyForCurrent);
        }
        return new UpdateAccountResponseDTO(account, hasChange, needsLogout, isOnlyForCurrent);
    }

    private UpdateClientResponseDTO updateClientDescriptionInner(UUID id, ClientUpdateDescriptionRequestDTO dto,
            AppUserDetails currentUser) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        clientSecurityService.validateCanUpdateClientDescription(currentUser, client);

        boolean hasChange = false;

        if (dto.extensionNumber() != null) {
            String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());
            if (Objects.equals(cleanExtensionNumber, client.getExtensionNumber())) {
                client.setExtensionNumber(cleanExtensionNumber);
                hasChange = true;
            }
        }
        if (dto.fullName() != null) {
            String cleanFullName = fullNameValidator.getCleanFullName(dto.fullName());
            if (Objects.equals(cleanFullName, client.getFullName())) {
                client.setFullName(cleanFullName);
                hasChange = true;
            }
        }

        return new UpdateClientResponseDTO(client, hasChange);
    }

    private Set<AccountAction> getAllActionsFromCode(Set<String> actionsCode) {
        return actionsCode.stream()
                .map(AccountAction::fromCode)
                .collect(Collectors.toSet());
    }

    private void updateAvatar(Client client, MultipartFile avatar) throws IOException {
        String avatarURL = avatarClientService.uploadClientAvatar(avatar, client.getId());
        client.setAvatarURL(avatarURL);
    }

    private void validateLogin(String login) {
        if (clientRepository.existsByAccount_Login(login)) {
            throw new DublicateClientLoginException(login);
        }
    }
}