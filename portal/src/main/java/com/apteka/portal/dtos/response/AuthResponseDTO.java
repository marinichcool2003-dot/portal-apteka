package com.apteka.portal.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Ответ аутентификации с токенами")
public record AuthResponseDTO(
    @Schema(description = "Access-токен")
    String accessToken,
    @Schema(description = "Refresh-токен")
    String refreshToken,
    @Schema(description = "Запомнить сессию")
    boolean rememberMe
) {}
