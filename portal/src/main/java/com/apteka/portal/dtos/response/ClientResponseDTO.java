package com.apteka.portal.dtos.response;

import java.util.Set;
import java.util.UUID;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

public record ClientResponseDTO(
    UUID id,
    String login,
    String fullName,
    Set<UserRole> role,
    String avatarURL,
    String groupClientName
) {
    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
            client.getId(), 
            client.getLogin(), 
            client.getFullName(), 
            client.getRoles(),
            client.getAvatarURL(),
            client.getUserGroup() != null ? client.getUserGroup().getName() : null
        );
    } 
}