package com.apteka.portal.dtos.response.auth;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ на запрос OTP")
public record OtpRequestResponseDTO(
        @Schema(description = "Сообщение для пользователя") String message) {

    public static final String GENERIC_MESSAGE =
            "Если учётная запись существует, код отправлен на email.";
}
