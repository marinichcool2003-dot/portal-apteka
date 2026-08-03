package com.apteka.portal.dtos.request.client;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание клиента")
public record ClientCreateRequestDTO(
        @Schema(description = "Логин пользователя")
        @NotBlank(message = "Логин не может быть пустым") @Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен") String login,
        @Schema(description = "Пароль")
        @NotBlank(message = "Пароль обязателен!") @Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,
        @Schema(description = "ФИО пользователя")
        @NotBlank(message = "ФИО обязательно") @Size(min = 2, max = 150, message = "Фио должно быть от 2 до 150 символов") @Pattern(regexp = "^[а-яА-Яa-zA-Z\\s\\-]+$", message = "ФИО не может содержать цифры или спецсимволы") String fullName,
        @Schema(description = "Номер телефона")
        @NotBlank(message = "Номер телефона обязателен") @Size(max = 20, message = "Номер телефона - максимум 20 символов") String phoneNumber,
        @Schema(description = "Код роли пользователя")
        @NotBlank(message = "Роль сотрудника обязательна!") String roleCode,
        @Schema(description = "Коды разрешённых действий аккаунта")
        Set<String> accountActionsCode,
        @Schema(description = "Добавочный номер")
        @Pattern(regexp = "^[0-9]+$", message = "Внутренний телефон должен содержать только цифры") String extensionNumber,
        @Schema(description = "Идентификатор группы клиента")
        @Positive(message = "Группа пользователя должна содержать только положительное число") Integer groupClientId) {
    public ClientCreateRequestDTO {
        if (accountActionsCode == null) {
            accountActionsCode = Set.of();
        }
    }
}
