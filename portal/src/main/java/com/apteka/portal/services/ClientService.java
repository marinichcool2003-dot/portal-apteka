package com.apteka.portal.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.components.AvatarClientService;
import com.apteka.portal.components.servicesecurity.ClientSecurityService;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;
import com.apteka.portal.components.validators.FullNameValidator;
import com.apteka.portal.components.validators.LoginValidator;
import com.apteka.portal.components.validators.PasswordValidator;
import com.apteka.portal.components.validators.PhoneNumberValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.response.AssignedStatsDTO;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.CreatedStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.dtos.response.UpdateBasicClientFormResponseDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.SelfDeleteException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.UserGroupRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final AuthService authService;
    private final ClientRepository clientRepository;
    private final AvatarClientService avatarClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupRepository userGroupRepository;
    private final TaskRepository taskRepository;
    private final ClientSecurityService clientSecurityService;
    private final PasswordValidator passwordValidator;
    private final LoginValidator loginValidator;
    private final FullNameValidator fullNameValidator;
    private final PhoneNumberValidator phoneNumberValidator;

    private final SseController sseController;

    @Value("${app.default.avatars.upload.dir}")
    private String uploadAvatarDir;

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAll(AppUserDetails currentUser, Pageable pageable, boolean isActive) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        return clientRepository.findAll(pageable, isActive).stream()
                .map(ClientResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public ClientResponseDTO getOne(UUID id, AppUserDetails currentUser) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
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
    public List<ClientResponseDTO> getByGroup(Integer userGroupId, AppUserDetails currentUser) {
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        if (!userGroupRepository.existsById(userGroupId))
            throw new GroupUserNotFoundException(userGroupId);
        return clientRepository.findByUserGroupId(userGroupId).stream()
                .map(ClientResponseDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ClientWithStatsDTO> getWithNumberOfTask(Integer userGroupId, AppUserDetails currentUser) {
        clientSecurityService.validateHasElevatedPrivelegesInGroup(currentUser, userGroupId);
        List<ClientResponseDTO> clients = getByGroup(userGroupId, currentUser);
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
    public ClientResponseDTO create(ClientRequestDTO dto, AppUserDetails currentUser) throws IOException {
        clientSecurityService.validateCanCreateClient(currentUser, dto.groupClientId());

        String cleanLogin = loginValidator.getCleanLogin(dto.login());
        validateLogin(dto.login());
        String normalizedName = fullNameValidator.getCleanFullName(dto.fullName());
        passwordValidator.validatePassword(dto.password(), true);
        String cleanPhoneNumber = phoneNumberValidator.getCleanPhoneNumber(dto.phoneNumber());
        String cleanExtensionNumber = phoneNumberValidator.getCleanExtensionNumber(dto.extensionNumber());

        UserGroup group = userGroupRepository.findById(dto.groupClientId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.groupClientId()));

        Set<UserRole> roles = dto.rolesCode()
                .stream()
                .map(UserRole::fromCode)
                .collect(Collectors.toSet());

        if (roles.size() == 0)
            roles.add(UserRole.USER);

        clientSecurityService.canGiveRoleToClient(roles, currentUser, group);

        Client newClient = Client.builder()
                .fullName(normalizedName)
                .roles(roles)
                .extensionNumber(cleanExtensionNumber)
                .avatarURL(uploadAvatarDir + "/default.png")
                .build();

        Account account = Account.builder()
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .phoneNumber(cleanPhoneNumber)
                .userGroup(group)
                .client(newClient)
                .build();

        newClient.setAccount(account);

        Client client = clientRepository.save(newClient);
        var signal = new SseEventNames.AppUserDetailsSignalDTO(newClient.getAccount().getUserGroup().getId(),
                SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
        return ClientResponseDTO.from(client);
    }

    @Transactional
    public ClientResponseDTO addRole(UUID id, String code, AppUserDetails currentUser) {
        UserRole role = UserRole.fromCode(code);
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        clientSecurityService.canGiveRoleToClient(Set.of(role), currentUser, client.getAccount().getUserGroup());

        if (client.getRoles().contains(role)) {
            return ClientResponseDTO.from(client);
        }

        client.getRoles().add(role);
        ClientResponseDTO response = ClientResponseDTO.from(client);
        sseController.sendNotification(client.getAccount().getLogin(), SseEventNames.REFRESH_CLIENTS, response);

        return response;
    }

    @Transactional
    public ClientResponseDTO removeRole(UUID id, String code, AppUserDetails currentUser) {
        UserRole role = UserRole.fromCode(code);
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        if (client.getRoles().size() <= 1) {
            throw new AccessDeniedException("Вы не можете удалить последнюю роль пользователя");
        }
        clientSecurityService.canRemoveRoles(Set.of(role), currentUser, client.getAccount().getUserGroup());
        client.getRoles().remove(role);
        ClientResponseDTO response = ClientResponseDTO.from(client);
        sseController.sendNotification(client.getAccount().getLogin(), SseEventNames.REFRESH_CLIENTS, response);
        return response;
    }

    @Transactional
    public ClientResponseDTO updateYourself(ClientUpdateRequestDTO dto, AppUserDetails currentUser)
            throws IOException {

        UpdateBasicClientFormResponseDTO savedClient = updateBasicClientForm(currentUser.getInternalId(), dto.login(), dto.password(),
                dto.avatar());
        Client client = savedClient.client();

        if (!savedClient.hasChange()) {
            return ClientResponseDTO.from(client);
        }

        client.setUpdatedBy(currentUser.getDisplayName());
        ClientResponseDTO response = ClientResponseDTO.from(client);
        sseController.sendNotification(client.getAccount().getLogin(), SseEventNames.REFRESH_CLIENTS, response);
        return response;
    }

    @Transactional
    public ClientResponseDTO fullUpdate(UUID id, FullClientUpdateRequestDTO dto, AppUserDetails currentUser)
            throws IOException {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может полностью изменять сотрудника");
        }

        UpdateBasicClientFormResponseDTO savedClient = updateBasicClientForm(id, dto.login(), dto.password(), dto.avatar());
        Client client = savedClient.client();
        boolean hasChange = savedClient.hasChange();

        if (StringUtils.hasText(dto.fullName())) {
            String cleanFullName = fullNameValidator.getCleanFullName(dto.fullName());
            if (!Objects.equals(cleanFullName, client.getFullName())) {
                client.setFullName(cleanFullName);
                hasChange = true;
            } 
        }

        if (dto.groupClientId() != null && dto.groupClientId() > 0
                && !Objects.equals(client.getAccount().getUserGroup().getId(), dto.groupClientId())) {
            List<AssignedStatsDTO> stats = taskRepository.getClientAssignedStatsBatch(List.of(client.getId()));
            AssignedStatsDTO thisClientStats = stats.stream()
                    .findFirst()
                    .orElse(new AssignedStatsDTO(client.getId(), 0L, 0L, 0L, 0L, 0L));
            if (thisClientStats.openCount() + thisClientStats.processedCount() > 0) {
                throw new AccessDeniedException("У пользователя еще имеются открытые задачи");
            }
            UserGroup group = userGroupRepository.findById(dto.groupClientId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.groupClientId()));
            client.getAccount().setUserGroup(group);
            hasChange = true;
        }

        if (hasChange) {
            client.setUpdatedBy(currentUser.getDisplayName());
            var signal = new SseEventNames.EntityUpdateSignalDTO(client.getId(), SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
        }
        return ClientResponseDTO.from(client);
    }

    @Transactional
    public void delete(UUID id, AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может удалять сотрудников");
        }
        if (Objects.equals(currentUser.getInternalId(), id)) {
            throw new SelfDeleteException("Вы не можете удалить самого себя!");
        }
        Client client = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        clientRepository.delete(client);
        var signal = new SseEventNames.AppUserDetailsSignalDTO(client.getAccount().getUserGroup().getId(),
                SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_CLIENTS, signal);
    }

    private UpdateBasicClientFormResponseDTO updateBasicClientForm(UUID id, String login, String password, MultipartFile avatar)
            throws IOException {
        Client upClient = clientRepository.findByIdWithAccount(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        String oldUserName = upClient.getAccount().getLogin();
        boolean needsLogout = false;
        boolean hasChange = false;

        if (avatar != null && !avatar.isEmpty()) {
            if (!Objects.equals(avatar.getOriginalFilename(), upClient.getAvatarURL())) {
                updateAvatar(upClient, avatar);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(login)) {
            String cleanLogin = loginValidator.getCleanLogin(login);
            if (!Objects.equals(cleanLogin, upClient.getAccount().getLogin())) {
                validateLogin(cleanLogin);
                upClient.getAccount().setLogin(cleanLogin);
                needsLogout = true;
                hasChange = true;
            }
        }

        if (StringUtils.hasText(password)) {
            if (passwordEncoder.matches(password, upClient.getAccount().getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            passwordValidator.validatePassword(password, true);
            upClient.getAccount().setPassword(passwordEncoder.encode(password));
            needsLogout = true;
            hasChange = true;
        }

        if (needsLogout) {
            authService.invalidateAllSession(oldUserName);
        }

        return new UpdateBasicClientFormResponseDTO(upClient, hasChange);
    }

    private void updateAvatar(Client client, MultipartFile avatar) throws IOException {
        String avatarURL = avatarClientService.uploadAvatar(avatar, client.getId());
        client.setAvatarURL(avatarURL);
    }

    private void validateLogin(String login) {
        if (clientRepository.existsByAccount_Login(login)) {
            throw new DublicateClientLoginException(login);
        }
    }
}