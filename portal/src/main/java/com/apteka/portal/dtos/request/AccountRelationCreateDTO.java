package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AccountRelationCreateDTO(
        @Positive(message = "Идентификатор группы должен быть положительным!")
        @NotNull(message = "Идентификатор не может быть пустым!")
        Integer userGroupIds,

        @NotBlank(message = "Код роли не может быть пустым!")
        @Size(max = 20, message = "Код роли не может быть длиннее 20 символов!")
        String userRoleCode,

        Set<String> actionCodes
) {
    public AccountRelationCreateDTO {
        if (actionCodes == null) {
            actionCodes = Set.of();
        }
    }
}
