package com.apteka.portal.dtos.response;

import java.util.UUID;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.Role;

public record ClientResponseDTO(
    UUID id,
    String login,
    String fullName,
    Role role,
    String avatarURL,
    String groupClientName
) {
    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
            client.getId(), 
            client.getLogin(), 
            client.getFullName(), 
            client.getRole(),
            client.getAvatarURL(),
            client.getGroupClient() != null ? client.getGroupClient().getName() : null
        );
    } 
}