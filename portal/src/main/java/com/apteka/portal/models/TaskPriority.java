package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskPriorityException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskPriority {
    LOW("LOW", "Низкий", "У задачи низкий приоритет"),
    MIDDLE("MIDDLE", "Средний", "У задачи средний приоритет"),
    HIGH("HIGH", "Высокий", "У задачи высокий приоритет");

    private final String code;
    private final String name;
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
