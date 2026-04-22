package com.apteka.portal.dtos.response;

import java.util.UUID;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.ClientRole;

public record ClientResponseDTO(
    UUID id,
    String login,
    String fullName,
    ClientRole role,
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
            client.getUserGroup() != null ? client.getUserGroup().getName() : null
        );
    } 
}