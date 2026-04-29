package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Pattern;

public record UserGroupRequestDTO(
        String name,
        @Pattern(
            regexp = "^\\+?[0-9]{7,15}$", 
            message = "Номер телефона должен быть в формате +123456789"
        ) 
        String phoneNumber
) {}
