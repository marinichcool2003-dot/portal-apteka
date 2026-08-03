package com.apteka.portal.dtos.request.client;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление описания клиента")
public record ClientUpdateDescriptionRequestDTO(
        @Schema(description = "ФИО пользователя")
        @NotEmpty(message = "Не может быть пустым!")
        String fullName,
        @Schema(description = "Добавочный номер")
        @Pattern(regexp = "^[0-9]+$", message = "Внутренний телефон должен содержать только цифры!")
        String extensionNumber
) {}
