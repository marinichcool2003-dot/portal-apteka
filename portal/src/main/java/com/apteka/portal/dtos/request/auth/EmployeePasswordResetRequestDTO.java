package com.apteka.portal.dtos.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос кода для сброса пароля сотрудника")
public record EmployeePasswordResetRequestDTO(
        @Schema(description = "Логин или email")
        @JsonAlias({ "login", "email" })
        @NotBlank(message = "Логин или email обязателен") String loginOrEmail) {
}
