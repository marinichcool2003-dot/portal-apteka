package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequestDTO(
    @NotBlank(message = "REFRESH_TOKEN не может быть пустым")
    String refreshToken
) {}
