package com.apteka.portal.dtos.request.apteka;

import com.apteka.portal.dtos.request.AccountRelationCreateDTO;
import com.apteka.portal.models.AccountRelation;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Запрос на создание аптеки")
public record AptekaRequestDTO(
        @Schema(description = "Логин пользователя (идентификатор без @)")
        @NotBlank(message = "Логин не может быть пустым")
        @Pattern(regexp = "^[a-zA-Z0-9_.-]{3,50}$", message = "Логин: 3–50 символов, буквы, цифры, _ . - (без @)") String login,

        @Schema(description = "Email (@farmp.ru)")
        @NotBlank(message = "Email обязателен")
        @Email(message = "Некорректный формат email")
        @Pattern(regexp = ".+@farmp\\.ru$", message = "Email должен быть в домене @farmp.ru") String email,

        @Schema(description = "Пароль")
        @NotBlank(message = "Пароль обязателен!")
        @Size(min = 8, message = "Пароль должен быть минимум 8 символов")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,

        @Schema(description = "Номер аптеки")
        @NotNull(message = "Номер аптеки обязателен к заполнению")
        @Positive(message = "Номер аптеки должен быть больше нуля")Integer number,

        @Schema(description = "Номер телефона")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789") String phoneNumber,

        @Schema(description = "Группы, рольи и права")
        @NotNull(message = "Группы, роли и права не должны быть пустыми")
        Set<@NotNull(message = "Отношения не должны быть пустыми") AccountRelationCreateDTO> accountRelations,

        @Schema(description = "Адрес аптеки")
        AdressRequestDTO adressRequestDTO) {
}
