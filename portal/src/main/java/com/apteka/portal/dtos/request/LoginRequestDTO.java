package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на аутентификацию")
public record LoginRequestDTO(
        @Schema(description = "Логин пользователя")
        @NotBlank(message = "Логин не может быть пустым") String login,
        @Schema(description = "Пароль")
        @NotBlank(message = "Пароль не может быть пустым") String password,
        @Schema(description = "Запомнить сессию")
        boolean rememberMe
) {}
