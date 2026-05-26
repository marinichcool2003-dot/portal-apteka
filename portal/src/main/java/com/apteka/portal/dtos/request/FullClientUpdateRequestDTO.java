package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record FullClientUpdateRequestDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "Логин не может быть пустым, но может быть null") String login,
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,
        MultipartFile avatar,
        @Pattern(regexp = "^(?!\\s*$).+", message = "Полное имя не может быть пустым, но может быть null") String fullName,
        @Positive(message = "Группа пользователя должна быть больше нуля") Integer groupClientId) {
}
