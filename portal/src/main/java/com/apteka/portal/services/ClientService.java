package com.apteka.portal.services;

import com.apteka.portal.repository.UserGroupInterface;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.InvalidClientLoginException;
import com.apteka.portal.exceptions.InvalidClientPasswordException;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.ClientRole;
import com.apteka.portal.repository.ClientInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService implements UserDetailsService{

    private final UserGroupService groupClientService;
    private final ClientInterface clientInterface;
    private final AvatarClientService avatarClientService;
    private final PasswordEncoder passwordEncoder;
    private final UserGroupService userGroupService;

    @Transactional(readOnly = true)
    public List<Client> getAll(){
        return clientInterface.findAll();
    }

    @Transactional(readOnly = true)
    public List<Client> getByRole(String code){
        ClientRole role = ClientRole.fromCode(code);
        return clientInterface.findByRole(role.name());
    }

    @Transactional(readOnly = true)
    public List<Client> getbyGroup(Integer userGroupId) {
        userGroupService.getOne(userGroupId);
        return clientInterface.findByGroupId(userGroupId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return clientInterface.findByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("Клиент не найден: " + login));
    }

    @Transactional
    public Client create(String login, String password, String fullName, String code, Integer groupClientId) throws IOException{
        System.out.println("SERVICE LOGIN = [" + login + "]");
        if (login == null || login.strip().isEmpty()) {
            throw new InvalidClientLoginException();
        }
        if (password == null || password.strip().isEmpty()) {
            throw new InvalidClientPasswordException();
        }

        // Проверяем, не существует ли уже пользователь с таким логином
        if (clientInterface.existsByLogin(login)) {
            throw new RuntimeException("Пользователь с логином " + login + " уже существует");
        }
        
        UserGroup group = groupClientService.getOne(groupClientId);
        ClientRole role = ClientRole.fromCode(code);

        Client newClient = Client.builder()
            .login(login)
            .password(passwordEncoder.encode(password)) // Кодируем пароль!
            .fullName(fullName)
            .role(role)
            .userGroup(group)
            .avatarURL("/uploads/avatars/clients/default.png")
            .build();
            
        return clientInterface.save(newClient);
    }

    @Transactional
    public Client updateRole(UUID id, String code) {
        Client upClient = getOne(id);
        ClientRole role = ClientRole.fromCode(code);
        upClient.setRole(role);
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client updateAvatar(UUID id, MultipartFile avatar) throws IOException{
        Client upClient = getOne(id);
        String avatarURL = avatarClientService.uploadAvatar(avatar, id);
        upClient.setAvatarURL(avatarURL);
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client updateAvatar(String username, MultipartFile avatar) throws IOException{
        Client upClient = clientInterface.findByLogin(username)
            .orElseThrow(() -> new ClientNotFoundException("Пользователь с именем " + username + " не найден!"));
        String avatarURL = avatarClientService.uploadAvatar(avatar, upClient.getId());
        upClient.setAvatarURL(avatarURL);
        return clientInterface.save(upClient);
    }

    @Transactional
    public Client update(UUID id, String login, String password, Integer groupClientId){
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
    public void delete(UUID id){
        if (clientInterface.existsById(id)) {
            clientInterface.deleteById(id);
        }
    }
}