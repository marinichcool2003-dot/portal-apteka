package com.apteka.portal.services;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.components.AvatarClientService;
import com.apteka.portal.components.ClientSecurityService;
import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.dtos.request.ClientUpdateRequestDTO;
import com.apteka.portal.dtos.request.FullClientUpdateRequestDTO;
import com.apteka.portal.components.PasswordValidator;
import com.apteka.portal.dtos.response.ClientResponseDTO;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidClientFullNameException;
import com.apteka.portal.exceptions.InvalidClientLoginException;
import com.apteka.portal.exceptions.SelfDeleteException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
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

    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getAll(AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("У вас нет прав на просмотр списка всех сотрудников");
        }
        return clientRepository.findAll().stream()
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

        Map<UUID, TaskStatsDTO> statsMap = taskRepository.getClientTaskStatsBatch(clientIds)
                .stream()
                .collect(Collectors.toMap(TaskStatsDTO::clientId, dto -> dto));

        return clients.stream()
                .map(clientDto -> new ClientWithStatsDTO(
                        clientDto,
                        statsMap.getOrDefault(clientDto.id(),
                                new TaskStatsDTO(clientDto.id(), 0L, 0L, 0L, 0L, 0L))))
                .toList();
    }

    @Transactional
    public ClientResponseDTO create(ClientRequestDTO dto, AppUserDetails currentUser) throws IOException {
        clientSecurityService.validateCanCreateClient(currentUser, dto.groupClientId());

        String cleanLogin = dto.login().strip();
        String normalizedName = dto.fullName() != null
                ? dto.fullName().replaceAll("[\\s_]+", " ").trim()
                : "";

        validateLogin(dto.login());
        if (normalizedName == null || normalizedName.isBlank()) {
            throw new InvalidClientFullNameException("ФИО не может быть пустым");
        }
        validateFullName(dto.fullName());
        passwordValidator.validatePassword(dto.password(), true);

        UserGroup group = userGroupRepository.findById(dto.groupClientId())
                .orElseThrow(() -> new GroupUserNotFoundException(dto.groupClientId()));

        Set<UserRole> roles = dto.rolesCode()
                .stream()
                .map(UserRole::fromCode)
                .collect(Collectors.toSet());

        clientSecurityService.canGiveRoleToClient(roles, currentUser, group);

        Client newClient = Client.builder()
                .login(cleanLogin)
                .password(passwordEncoder.encode(dto.password()))
                .fullName(normalizedName)
                .roles(roles)
                .userGroup(group)
                .avatarURL("/uploads/avatars/clients/default.png")
                .build();

        Client client = clientRepository.save(newClient);
        return ClientResponseDTO.from(client);
    }

    public Client addRole(UUID id, String code, AppUserDetails currentUser) {
        UserRole role = UserRole.fromCode(code);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        clientSecurityService.canGiveRoleToClient(Set.of(role), currentUser, client.getUserGroup());
        client.getRoles().add(role);
        return clientRepository.save(client);
    }

    public Client removeRole(UUID id, String code) {
        UserRole role = UserRole.fromCode(code);
        if (role == UserRole.USER) {
            throw new AccessDeniedException("Вы не можете удалить стандартную роль пользователя");
        }
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        client.getRoles().remove(role);
        return clientRepository.save(client);
    }

    @Transactional
    public ClientResponseDTO updateYourself(UUID id, ClientUpdateRequestDTO dto, AppUserDetails currentUser)
            throws IOException {
        if (!Objects.equals(currentUser.getClientId(), id)) {
            throw new AccessDeniedException("Вы не можете изменять не свой профиль");
        }

        Client savedClient = updateBasicClientForm(id, dto.login(), dto.password(), dto.avatar());

        return ClientResponseDTO.from(savedClient);
    }

    @Transactional
    public ClientResponseDTO fullUpdate(UUID id, FullClientUpdateRequestDTO dto, AppUserDetails currentUser)
            throws IOException {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может полностью изменять сотрудника");
        }

        Client savedClient = updateBasicClientForm(id, dto.login(), dto.password(), dto.avatar());

        if (dto.fullName() != null && !dto.fullName().isBlank()) {
            validateFullName(dto.fullName());
            savedClient.setFullName(dto.fullName());
        }

        if (dto.groupClientId() != null && !Objects.equals(savedClient.getUserGroup().getId(), dto.groupClientId())) {
            List<TaskStatsDTO> stats = taskRepository.getClientTaskStatsBatch(List.of(savedClient.getId()));
            TaskStatsDTO thisClientStats = stats.stream()
                    .findFirst()
                    .orElse(new TaskStatsDTO(savedClient.getId(), 0L, 0L, 0L, 0L, 0L));
            if (thisClientStats.openCount() + thisClientStats.processedCount() > 0) {
                throw new AccessDeniedException("У пользователя еще имеются открытые задачи");
            }
            UserGroup group = userGroupRepository.findById(dto.groupClientId())
                    .orElseThrow(() -> new GroupUserNotFoundException(dto.groupClientId()));
            savedClient.setUserGroup(group);
        }

        return ClientResponseDTO.from(savedClient);
    }

    @Transactional
    public void delete(UUID id, AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может удалять сотрудников");
        }
        if (Objects.equals(currentUser.getClientId(), id)) {
            throw new SelfDeleteException("Вы не можете удалить самого себя!");
        }
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        }
    }

    private Client updateBasicClientForm(UUID id, String login, String password, MultipartFile avatar)
            throws IOException {
        Client upClient = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));

        String oldUserName = upClient.getLogin();
        boolean needsLogout = false;

        if (avatar != null && !avatar.isEmpty()) {
            updateAvatar(upClient, avatar);
        }

        if (login != null && !login.isBlank()) {
            String newLogin = login.strip();
            if (!newLogin.equals(upClient.getLogin())) {
                validateLogin(newLogin);
                upClient.setLogin(newLogin);
                needsLogout = true;
            }
        }

        if (password != null && !password.isBlank()) {
            if (passwordEncoder.matches(password, upClient.getPassword())) {
                throw new AlreadyHaveThisPasswordException();
            }
            passwordValidator.validatePassword(password, true);
            upClient.setPassword(passwordEncoder.encode(password));
            needsLogout = true;
        }

        if (needsLogout) {
            authService.invalidateAllSession(oldUserName);
        }

        return upClient;
    }

    private void updateAvatar(Client client, MultipartFile avatar) throws IOException {
        String avatarURL = avatarClientService.uploadAvatar(avatar, client.getId());
        client.setAvatarURL(avatarURL);
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new InvalidClientLoginException();
        }
        if (clientRepository.existsByLogin(login)) {
            throw new DublicateClientLoginException(login);
        }
        if (!login.contains("@farmp.ru")) {
            throw new InvalidClientLoginException("Логин должен содержать домен");
        }
    }

    private void validateFullName(String normalizedName) {
        if (normalizedName.length() > 100) {
            throw new InvalidClientFullNameException("ФИО не может быть длиннее 100 символов");
        }

        if (!normalizedName.matches("^[\\p{L}'-]+(?:\\s[\\p{L}'-]+){1,2}$")) {
            throw new InvalidClientFullNameException("Введите корректные Фамилию и Имя (или ФИО)");
        }
    }
}