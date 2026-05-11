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
import com.apteka.portal.dtos.request.PasswordValidator;
import com.apteka.portal.dtos.response.ClientWithStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.exceptions.AlreadyHaveThisPasswordException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.DublicateClientLoginException;
import com.apteka.portal.exceptions.InvalidClientFullNameException;
import com.apteka.portal.exceptions.InvalidClientLoginException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final AuthService authService;
    private final UserGroupService groupClientService;
    private final ClientRepository clientRepository;
    private final AvatarClientService avatarClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupService userGroupService;
    private final TaskRepository taskRepository;
    private final ClientSecurityService clientSecurityService;
    private final PasswordValidator passwordValidator;

    @Transactional(readOnly = true)
    public List<Client> getAll() {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("У вас нет прав на просмотр списка всех сотрудников");
        }
        return clientRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Client getOne(UUID id) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        return clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Client> getbyGroup(Integer userGroupId) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        clientSecurityService.validateWhoCanSelectClients(currentUser);
        userGroupService.getOne(userGroupId);
        return clientRepository.findByUserGroupId(userGroupId);
    }

    @Transactional(readOnly = true)
    public List<ClientWithStatsDTO> getWithNumberOfTask(Integer userGroupId) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        clientSecurityService.validateHasElevatedPrivelegesInGroup(currentUser, userGroupId);
        List<Client> clients = getbyGroup(userGroupId);
        if (clients.isEmpty())
            return List.of();

        List<UUID> clientIds = clients.stream().map(Client::getId).toList();

        Map<UUID, TaskStatsDTO> statsMap = taskRepository.getClientTaskStatsBatch(clientIds)
                .stream()
                .collect(Collectors.toMap(TaskStatsDTO::clientId, dto -> dto));

        return clients.stream()
                .map(client -> new ClientWithStatsDTO(
                        client,
                        statsMap.getOrDefault(client.getId(),
                                new TaskStatsDTO(client.getId(), 0L, 0L, 0L, 0L, 0L))))
                .toList();
    }

    @Transactional
    public Client create(ClientRequestDTO dto) throws IOException {

        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        clientSecurityService.validateCanCreateClient(currentUser, dto.groupClientId());

        validateLogin(dto.login());
        passwordValidator.validatePassword(dto.password(), true);
        validateFullName(dto.fullName());

        String cleanLogin = dto.login().strip();
        String normalizedName = dto.fullName().trim().replaceAll("\\s+", " ");

        UserGroup group = groupClientService.getOne(dto.groupClientId());

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

        return clientRepository.save(newClient);
    }

    public Client addRole(UUID id, String code) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        UserRole role = UserRole.fromCode(code);
        Client client = getOne(id);
        clientSecurityService.canGiveRoleToClient(Set.of(role), currentUser, client.getUserGroup());
        client.getRoles().add(role);
        return clientRepository.save(client);
    }

    public Client removeRole(UUID id, String code) {
        UserRole role = UserRole.fromCode(code);
        if (role == UserRole.USER) {
            throw new AccessDeniedException("Вы не можете удалить стандартную роль пользователя");
        }
        Client client = getOne(id);
        client.getRoles().remove(role);
        return clientRepository.save(client);
    }

    @Transactional
    public Client updateYourself(UUID id, ClientUpdateRequestDTO dto) throws IOException {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();

        if (!Objects.equals(currentUser.getClientId(), id)) {
            throw new AccessDeniedException("Вы не можете изменять не свой профиль");
        }

        Client savedClient = updateBasicClientForm(id, dto.login(), dto.password(), dto.avatar());

        return savedClient;
    }

    @Transactional
    public Client fullUpdate(UUID id, FullClientUpdateRequestDTO dto) throws IOException{
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может полностью изменять сотрудника");
        }

        Client savedClient = updateBasicClientForm(id, dto.login(), dto.password(), dto.avatar());

        validateFullName(dto.fullName());
        savedClient.setFullName(dto.fullName());

        if (!Objects.equals(savedClient.getUserGroup().getId(), dto.groupClientId())) {
            List<TaskStatsDTO> stats = taskRepository.getClientTaskStatsBatch(List.of(savedClient.getId()));
            TaskStatsDTO thisClientStats = stats.get(0);
            if (thisClientStats.openCount() + thisClientStats.processedCount() > 0) {
                throw new AccessDeniedException("У пользователя еще имеются открытые задачи");
            }
            UserGroup group = userGroupService.getOne(dto.groupClientId());
            savedClient.setUserGroup(group);
        }

        return savedClient;
    }

    @Transactional
    public void delete(UUID id) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может удалять сотрудников");
        }
        if (clientRepository.existsById(id)) {
            clientRepository.deleteById(id);
        }
    }

    private Client updateBasicClientForm(UUID id, String login, String password, MultipartFile avatar) throws IOException{
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

        Client savedClient = clientRepository.save(upClient);

        if (needsLogout) {
            authService.invalidateAllSession(oldUserName);
        }

        return savedClient;
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
    }

    private void validateFullName(String fullName) {
        if (fullName == null) {
            throw new InvalidClientFullNameException("ФИО не может быть пустым");
        }
        String trimmed = fullName.trim();

        if (!trimmed.matches("^[а-яА-Яa-zA-Z\\s\\-]+$")) {
            throw new InvalidClientFullNameException("ФИО может содержать только буквы, пробелы и дефисы");
        }

        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            throw new InvalidClientFullNameException("Введите фамилию и имя полностью");
        }

        if (trimmed.equals(trimmed.toUpperCase()) && trimmed.length() < 5) {
            throw new InvalidClientFullNameException("ФИО не должно быть написано только заглавными буквами");
        }
    }
}