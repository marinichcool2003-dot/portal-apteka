package com.apteka.portal.dtos.request.auth;

import com.fasterxml.jackson.annotation.JsonAlias;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Подтверждение входа аптеки по коду")
public record AptekaCodeLoginConfirmDTO(
        @Schema(description = "Логин или email")
        @JsonAlias({ "login", "email" })
        @NotBlank(message = "Логин или email обязателен") String loginOrEmail,
        @Schema(description = "OTP-код из письма")
        @NotBlank(message = "Код подтверждения обязателен") @Pattern(regexp = "^[0-9]{6}$", message = "Код должен состоять из 6 цифр") String code) {
}
