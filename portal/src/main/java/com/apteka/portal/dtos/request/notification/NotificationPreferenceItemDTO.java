package com.apteka.portal.dtos.request.notification;

import com.apteka.portal.models.NotificationChannel;
import com.apteka.portal.models.NotificationEventType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Элемент настройки уведомления")
public record NotificationPreferenceItemDTO(
        @Schema(description = "Канал доставки")
        @NotNull(message = "Канал обязателен") NotificationChannel channel,
        @Schema(description = "Тип события")
        @NotNull(message = "Тип события обязателен") NotificationEventType eventType,
        @Schema(description = "Включено") boolean enabled) {
}
