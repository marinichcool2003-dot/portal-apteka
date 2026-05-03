package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserGroupRequestDTO(
        @NotBlank(message = "Наименование группы не может быть пустым")
        String name,

        @Pattern(
            regexp = "^\\+?[0-9]{7,15}$", 
            message = "Номер телефона должен быть в формате +123456789"
        )
        String phoneNumber
) {}
