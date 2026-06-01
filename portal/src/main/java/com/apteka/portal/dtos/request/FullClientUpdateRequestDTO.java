package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record FullClientUpdateRequestDTO(
                @Schema(description = "Логин пользователя", example = "ivan.ivanov@farmp.ru") @Pattern(regexp = "^(?!\\s*$).+", message = "Логин не может быть пустым, но может быть null") String login,
                @Schema(description = "Пароль пользователя", example = "password123G!", format = "password") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,
                @Schema(description = "Аватар пользователя", type = "string", format = "binary") MultipartFile avatar,
                @Schema(description = "Полное имя пользователя", example = "Иванов Иван Иванович") @Pattern(regexp = "^(?!\\s*$).+", message = "Полное имя не может быть пустым, но может быть null") String fullName,
                @Schema(description = "ID группы пользователя", example = "1") @Positive(message = "Группа пользователя должна быть больше нуля") Integer groupClientId) {
}
