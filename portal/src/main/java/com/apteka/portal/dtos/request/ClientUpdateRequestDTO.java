package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record ClientUpdateRequestDTO(
        @NotEmpty(message = "Не может быть пустым!")
        String fullName,

        @Pattern(regexp = "^[0-9]+$", message = "Внутренний телефон должен содержать только цифры")
        String extensionNumber
        
        // @Pattern(
        //         regexp = "^[^\\s]+$",
        //         message = "Ссылка не должна содержать пробелы"
        // )
        // String avatarUrl
) {}
