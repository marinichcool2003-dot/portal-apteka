package com.apteka.portal.dtos.response.client;

import java.util.UUID;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Ответ с данными клиента")
public record ClientResponseDTO(
        @Schema(description = "Идентификатор")
        UUID id,
        @Schema(description = "Логин пользователя")
        String login,
        @Schema(description = "Номер телефона")
        String phoneNumber,
        @Schema(description = "Добавочный номер")
        String extensionNumber,
        @Schema(description = "ФИО пользователя")
        String fullName,
        @Schema(description = "Роль пользователя")
        UserRole role,
        @Schema(description = "URL аватара")
        String avatarURL,
        @Schema(description = "Группа пользователей")
        UserGroupShortResponseDTO userGroup,
        @Schema(description = "Признак доступности учётной записи")
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
                group != null ? UserGroupShortResponseDTO.from(group) : null,
                isEnabled);
    }
}