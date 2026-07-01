package com.apteka.portal.dtos.request.client;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ClientUpdateDescriptionRequestDTO(
        @NotEmpty(message = "Не может быть пустым!")
        String fullName,

        @Pattern(regexp = "^[0-9]+$", message = "Внутренний телефон должен содержать только цифры!")
        String extensionNumber
) {}
