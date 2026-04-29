package com.apteka.portal.dtos.request;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientRequestDTO(
        String login,
        @NotBlank(message = "Пароль обязателен!") 
        @Size(min = 8, message = "Пароль должен быть минимум 8 символов") 
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", 
            message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") 
        String password,
        String fullName,
        Set<String> rolesCode,
        Integer groupClientId) {
}
