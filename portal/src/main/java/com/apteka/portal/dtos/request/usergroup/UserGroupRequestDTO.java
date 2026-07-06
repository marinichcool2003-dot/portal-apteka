package com.apteka.portal.dtos.request.usergroup;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserGroupRequestDTO(
        @NotBlank(message = "Наименование группы не может быть пустым")
        String name,

        @Pattern(
            regexp = "^\\+?[0-9]{7,15}$", 
            message = "Номер телефона должен быть в формате +123456789"
        )
        String phoneNumber,

        @Pattern(regexp = "^[0-9]{1,20}$")
        String internalNumber,

        @Pattern(regexp = "^[0-9]{1,20}$")
        String extensionNumber,

        Set<Integer> visibleGroups
) implements UserGroupRequestInterface {}
