package com.apteka.portal.dtos.response.client;

import java.util.UUID;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;

public record ClientResponseDTO(
        UUID id,
        String login,
        String phoneNumber,
        String extensionNumber,
        String fullName,
        UserRole role,
        String avatarURL,
        UserGroupShortResponseDTO userGroup,
        Boolean isEnabled) {
    public static ClientResponseDTO from(Client client) {
        Account account = client.getAccount();

        if (account == null) {
            return new ClientResponseDTO(
                    client.getId(),
                    null, null,
                    client.getExtensionNumber(),
                    client.getFullName(),
                    null, null, null,
                    false);
        }

        UserGroup group = account.getUserGroup();

        boolean isAccountActive = Boolean.TRUE.equals(account.isActive());

        boolean isGroupActive = (group == null) || Boolean.TRUE.equals(group.isActive());

        boolean isEnabled = isAccountActive && isGroupActive;

        return new ClientResponseDTO(
                client.getId(),
                account.getLogin(),
                account.getPhoneNumber(),
                client.getExtensionNumber(),
                client.getFullName(),
                account.getUserRole(),
                client.getAvatarURL(),
                group != null ? new UserGroupShortResponseDTO(group.getId(), group.getName()) : null,
                isEnabled);
    }
}