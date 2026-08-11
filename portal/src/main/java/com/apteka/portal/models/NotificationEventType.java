package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Тип события уведомления")
@Getter
@AllArgsConstructor
public enum NotificationEventType {
    @Schema(description = "Задача назначена")
    TASK_ASSIGNED("TASK_ASSIGNED", "Задача назначена"),
    @Schema(description = "Изменён статус задачи")
    TASK_STATUS_CHANGED("TASK_STATUS_CHANGED", "Изменён статус задачи"),
    @Schema(description = "Комментарий к задаче")
    TASK_COMMENT("TASK_COMMENT", "Комментарий к задаче"),
    @Schema(description = "Ежедневный отчёт по отделу")
    DAILY_DEPARTMENT_REPORT("DAILY_DEPARTMENT_REPORT", "Ежедневный отчёт по отделу"),
    @Schema(description = "Новость")
    NEWS("NEWS", "Новость");

    private final String code;
    private final String description;

    public static NotificationEventType fromCode(String code) {
        for (NotificationEventType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Неизвестный тип события уведомления: " + code);
    }
}
