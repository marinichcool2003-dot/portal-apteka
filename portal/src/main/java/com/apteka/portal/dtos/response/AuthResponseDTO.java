package com.apteka.portal.dtos.response;

public record AuthResponseDTO(
    String accessToken,
    String refreshToken
) {}
