package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Канал доставки уведомления")
@Getter
@AllArgsConstructor
public enum NotificationChannel {
    @Schema(description = "Server-Sent Events")
    SSE("SSE", "SSE"),
    @Schema(description = "Email")
    EMAIL("EMAIL", "Email");

    private final String code;
    private final String description;

    public static NotificationChannel fromCode(String code) {
        for (NotificationChannel channel : values()) {
            if (channel.code.equals(code)) {
                return channel;
            }
        }
        throw new IllegalArgumentException("Неизвестный канал уведомления: " + code);
    }
}
