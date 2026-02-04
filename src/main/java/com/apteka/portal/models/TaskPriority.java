package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskPriority {
    LOW("Низкий"),
    MIDDLE("Средний"),
    HIGH("Высокий");

    private final String description;

    public static TaskPriority fromDescription(String description) {
        for (TaskPriority priority : values()) {
            if (priority.description.equals(description)) {
                return priority;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + description);
    }
}
