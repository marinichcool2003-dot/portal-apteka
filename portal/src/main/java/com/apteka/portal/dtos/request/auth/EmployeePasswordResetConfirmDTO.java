package com.apteka.portal.dtos.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Подтверждение сброса пароля сотрудника")
public record EmployeePasswordResetConfirmDTO(
        @Schema(description = "Логин или email")
        @JsonAlias({ "login", "email" })
        @NotBlank(message = "Логин или email обязателен") String loginOrEmail,
        @Schema(description = "OTP-код из письма")
        @NotBlank(message = "Код подтверждения обязателен") @Pattern(regexp = "^[0-9]{6}$", message = "Код должен состоять из 6 цифр") String code,
        @Schema(description = "Новый пароль")
        @NotBlank(message = "Пароль обязателен") @Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String newPassword) {
}
