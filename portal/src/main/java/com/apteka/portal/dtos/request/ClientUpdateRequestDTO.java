package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequestDTO(
        @Schema(description = "Логин пользователя", example = "ivan.ivanov@farmp.ru") 
        @Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен") 
        String login,
        
        @Schema(description = "Пароль пользователя", example = "password123G!", format = "password") 
        @Size(min = 8, message = "Пароль должен быть минимум 8 символов") 
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") 
        String password,
        
        @Schema(description = "Аватар пользователя", type = "string", format = "binary") 
        MultipartFile avatar
) {}
