package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientUpdateRequestDTO(
    @Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен")
    String login,
    @Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол")
    String password,
    MultipartFile avatar
) 
{}
