package com.apteka.portal.services;

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
import com.apteka.portal.models.GroupClient;
import com.apteka.portal.models.Role;
import com.apteka.portal.repository.ClientInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClientService implements UserDetailsService{

    private final GroupClientService groupClientService;
    private final ClientInterface clientInterface;
    private final AvatarClientService avatarClientService;
    private final PasswordEncoder passwordEncoder; 

    @Transactional(readOnly = true)
    public List<Client> getAll(){
        return clientInterface.findAll();
    }

    @Transactional(readOnly = true)
    public List<Client> findByRole(String code){
        Role role = Role.fromCode(code);
        return clientInterface.findByRole(role.name());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        return clientInterface.findByLogin(login)
            .orElseThrow(() -> new UsernameNotFoundException("Клиент не найден: " + login));
    }

    @Transactional(readOnly = true)
    public Client getOne(UUID id){
        return clientInterface.findById(id)
            .orElseThrow(() -> new ClientNotFoundException(id));
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
        
        GroupClient group = groupClientService.getOne(groupClientId);
        Role role = Role.fromCode(code);

        Client newClient = Client.builder()
            .login(login)
            .password(passwordEncoder.encode(password)) // Кодируем пароль!
            .fullName(fullName)
            .role(role)
            .groupClient(group)
            .avatarURL("/uploads/avatars/clients/default.png")
            .build();
            
        return clientInterface.save(newClient);
    }

    @Transactional
    public Client updateRole(UUID id, String code) {
        Client upClient = getOne(id);
        Role role = Role.fromCode(code);
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
            GroupClient groupClient = groupClientService.getOne(groupClientId);
            upClient.setGroupClient(groupClient);
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