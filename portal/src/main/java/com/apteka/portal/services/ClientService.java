package com.apteka.portal.services;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

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
public class ClientService {

    private final GroupClientService groupClientService;
    private final ClientInterface clientInterface;
    private final AvatarClientService avatarClientService;

    @Transactional(readOnly = true)
    public List<Client> getAll(){
        return clientInterface.findAll();
    }

    @Transactional(readOnly = true)
    public List<Client> findByRole(String code){
        Role role = Role.fromCode(code);
        return clientInterface.findByRole(role.name());
    }

    @SuppressWarnings("null")
    @Transactional(readOnly = true)
    public Client getOne(UUID id){
        return clientInterface.findById(id)
            .orElseThrow(() -> new ClientNotFoundException(id));
    }

    @SuppressWarnings("null")
    @Transactional
    public Client create(String login, String password, String fullName, String code, GroupClient groupClient) throws IOException{
        if (login.strip().isEmpty() || login == null) throw new InvalidClientLoginException();
        if (password.strip().isEmpty() || password == null) throw new InvalidClientPasswordException();
        GroupClient group = groupClientService.getOne(groupClient.getId());
        Role role = Role.fromCode(code);

        Client newClient = Client.builder()
            .login(login)
            .password(password)
            .fullName(fullName)
            .role(role)
            .group(group)
            .avatarURL("/uploads/avatars/clients/default.png")
            .build();
        return clientInterface.save(newClient);
    }

    @Transactional
    public Client updateAvatar(UUID id, MultipartFile avatar) throws IOException{
        Client upClient = getOne(id);

        String avatarURL = avatarClientService.uploadAvatar(avatar, id);
        upClient.setAvatarURL(avatarURL);

        return clientInterface.save(upClient);
    }

    @SuppressWarnings("null")
    @Transactional
    public Client update(UUID id, String login, String password, String code, GroupClient groupClient){
        Client upClient = getOne(id);
        groupClientService.getOne(groupClient.getId());
        if (login != null || !login.isEmpty()) {
            upClient.setLogin(login);
        }
        if (password != null || !password.isEmpty()) {
            upClient.setLogin(password);
        }
        if (code != null || !code.isEmpty()) {
            upClient.setLogin(code);
        }
        return clientInterface.save(upClient);
    }

    @SuppressWarnings("null")
    @Transactional
    public void delete(UUID id){
        if (clientInterface.existsById(id)) {
            clientInterface.deleteById(id);
        }
    }
}
