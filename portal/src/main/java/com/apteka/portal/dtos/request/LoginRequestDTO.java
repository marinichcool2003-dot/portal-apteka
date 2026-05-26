package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record LoginRequestDTO(
    @NotBlank(message = "Логин не может быть пустым") String login,
    @NotBlank(message = "Пароль не может быть пустым") String password,
    boolean rememberMe
) {}
