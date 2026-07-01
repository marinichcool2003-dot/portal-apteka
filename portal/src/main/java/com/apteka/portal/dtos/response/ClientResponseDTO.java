package com.apteka.portal.dtos.response;

import java.util.Optional;
import java.util.UUID;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

public record ClientResponseDTO(
        UUID id,
        String login,
        String fullName,
        UserRole role,
        String avatarURL,
        UserGroupShortResponseDTO userGroup) {
    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getAccount().getLogin(),
                client.getFullName(),
                client.getAccount().getUserRole(),
                client.getAvatarURL(),
                Optional.ofNullable(client.getAccount().getUserGroup())
                        .map(group -> new UserGroupShortResponseDTO(group.getId(), group.getName()))
                        .orElse(null));
    }
}