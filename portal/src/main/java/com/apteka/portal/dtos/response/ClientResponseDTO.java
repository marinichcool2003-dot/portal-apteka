package com.apteka.portal.dtos.response;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

public record ClientResponseDTO(
        UUID id,
        String login,
        String fullName,
        Set<UserRole> roles,
        String avatarURL,
        UserGroupShortResponseDTO userGroup) {
    public static ClientResponseDTO from(Client client) {
        return new ClientResponseDTO(
                client.getId(),
                client.getLogin(),
                client.getFullName(),
                client.getRoles(),
                client.getAvatarURL(),
                Optional.ofNullable(client.getUserGroup())
                        .map(group -> new UserGroupShortResponseDTO(group.getId(), group.getName()))
                        .orElse(null));
    }
}