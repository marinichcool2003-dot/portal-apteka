package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskPriorityException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskPriority {
    LOW("LOW", "Низкий"),
    MIDDLE("MIDDLE", "Средний"),
    HIGH("HIGH", "Высокий");

    private final String code;
    private final String description;

    public static TaskPriority fromCode(String code) {
        for (TaskPriority priority : values()) {
            if (priority.code.equals(code)) {
                return priority;
            }
        }
        throw new UnknowTaskPriorityException("Неизвестный статус: " + code);
    }
}
