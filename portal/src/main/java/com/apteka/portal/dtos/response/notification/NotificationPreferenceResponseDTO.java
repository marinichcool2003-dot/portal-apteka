package com.apteka.portal.dtos.response.notification;

import java.util.List;

import com.apteka.portal.dtos.request.notification.NotificationPreferenceItemDTO;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Настройки уведомлений пользователя")
public record NotificationPreferenceResponseDTO(
        @Schema(description = "Список настроек") List<NotificationPreferenceItemDTO> preferences) {
}
