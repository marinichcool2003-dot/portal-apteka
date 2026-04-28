package com.apteka.portal.services;

import com.apteka.portal.repository.UserGroupInterface;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.ClientRequestDTO;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.InvalidClientLoginException;
import com.apteka.portal.exceptions.InvalidClientPasswordException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;
import com.apteka.portal.models.UsersInApp;
import com.apteka.portal.repository.ClientInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final UserGroupService groupClientService;
    private final ClientInterface clientInterface;
    private final AvatarClientService avatarClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupService userGroupService;

    @Transactional(readOnly = true)
    public List<Client> getAll() {
        return clientInterface.findAll();
    }

    @Transactional(readOnly = true)
    public Client getOne(UUID id) {
        return clientInterface.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Client> getbyGroup(Integer userGroupId) {
        userGroupService.getOne(userGroupId);
        return clientInterface.findByGroupId(userGroupId);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('SENIOR','BOSS','ADMIN')")
    public Client create(ClientRequestDTO dto) throws IOException {

        AppUserDetails currentUser = SecurityUtils.getCurrentUser();

        if (dto.login() == null || dto.login().isBlank()) {
            throw new InvalidClientLoginException();
        }
        if (dto.password() == null || dto.password().isBlank()) {
            throw new InvalidClientPasswordException();
        }

        if (clientInterface.existsByLogin(dto.login().strip())) {
            throw new RuntimeException("Пользователь с логином " + dto.login() + " уже существует");
        }

        UserGroup group = groupClientService.getOne(dto.groupClientId());

        Set<UserRole> roles = dto.rolesCode()
                .stream()
                .map(UserRole::fromCode)
                .collect(Collectors.toSet());

        
        canGiveRoleToClient(roles, currentUser, group);

        Client newClient = Client.builder()
                .login(dto.login())
                .password(passwordEncoder.encode(dto.password()))
                .fullName(dto.fullName())
                .roles(roles)
                .userGroup(group)
                .avatarURL("/uploads/avatars/clients/default.png")
                .build();

        return clientInterface.save(newClient);
    }

    private void canGiveRoleToClient(Set<UserRole> newRoles, AppUserDetails currentUser, UserGroup targetGroup) {

        if (newRoles == null || newRoles.isEmpty()) {
            throw new IllegalArgumentException("Роль обязательна");
        }

        UserRole maxRole = currentUser.getRoles().stream()
                .max(Comparator.comparingInt(UserRole::getLevel))
                .orElse(UserRole.USER);

        if (maxRole == UserRole.USER) {
            throw new AccessDeniedException("USER не может назначать роли");
        }

        boolean sameGroup = Objects.equals(currentUser.getUserGroup().getId(), targetGroup.getId());

        for (UserRole role : newRoles) {
            if (role.getLevel() >= maxRole.getLevel()) {
                throw new AccessDeniedException("Нельзя назначать роль выше или равную своей");
            }
            if (maxRole != UserRole.ADMIN && !sameGroup) {
                throw new AccessDeniedException("Можно работать только в своей группе");
            }
        }
    }

    @Transactional // Переписать
    public Client updateRole(UUID id, String code) {
        Client upClient = getOne(id);
        UserRole role = UserRole.fromCode(code);
        upClient.setRoles(Set.of(role));
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client updateAvatar(UUID id, MultipartFile avatar) throws IOException {
        Client upClient = getOne(id);
        String avatarURL = avatarClientService.uploadAvatar(avatar, id);
        upClient.setAvatarURL(avatarURL);
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client updateAvatar(String username, MultipartFile avatar) throws IOException {
        Client upClient = clientInterface.findByLogin(username)
                .orElseThrow(() -> new ClientNotFoundException("Пользователь с именем " + username + " не найден!"));
        String avatarURL = avatarClientService.uploadAvatar(avatar, upClient.getId());
        upClient.setAvatarURL(avatarURL);
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client update(UUID id, String login, String password, Integer groupClientId) {
        Client upClient = getOne(id);

        if (groupClientId != null) {
            UserGroup groupClient = groupClientService.getOne(groupClientId);
            upClient.setUserGroup(groupClient);
        }

        if (login != null && !login.isEmpty()) {
            upClient.setLogin(login);
        }

        if (password != null && !password.isEmpty()) {
            upClient.setPassword(passwordEncoder.encode(password));
        }

        return clientInterface.save(upClient);
    }

    @Transactional
    public void delete(UUID id) {
        if (clientInterface.existsById(id)) {
            clientInterface.deleteById(id);
        }
    }
}