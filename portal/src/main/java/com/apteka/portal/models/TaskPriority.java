package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskPriorityException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Приоритет задачи")
@AllArgsConstructor
@Getter
public enum TaskPriority {
    @Schema(description = "Низкий")
    LOW("LOW", "Низкий", "У задачи низкий приоритет"),
    @Schema(description = "Средний")
    MIDDLE("MIDDLE", "Средний", "У задачи средний приоритет"),
    @Schema(description = "Высокий")
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
