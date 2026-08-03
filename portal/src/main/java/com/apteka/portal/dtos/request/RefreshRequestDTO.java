package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление токена доступа")
public record RefreshRequestDTO(
        @Schema(description = "Refresh-токен")
        @NotBlank(message = "REFRESH_TOKEN не может быть пустым")
        String refreshToken
) {}
