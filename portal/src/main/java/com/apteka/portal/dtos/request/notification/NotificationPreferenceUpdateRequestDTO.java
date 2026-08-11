package com.apteka.portal.dtos.request.notification;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Запрос обновления настроек уведомлений")
public record NotificationPreferenceUpdateRequestDTO(
        @Schema(description = "Список настроек")
        @NotNull(message = "Список настроек обязателен") @Valid List<NotificationPreferenceItemDTO> preferences) {
}
