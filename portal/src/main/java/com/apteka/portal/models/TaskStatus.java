package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskStatusException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Статус задачи")
@AllArgsConstructor
@Getter
public enum TaskStatus {
    @Schema(description = "Открыта")
    OPEN("OPEN", "Открыта", "Задача открыта"),
    @Schema(description = "Закрыта")
    CLOSED("CLOSED", "Закрыта", "Задача выполнена"),
    @Schema(description = "Отклонена")
    DENIED("DENIED", "Отклонена", "Задача отклонена"),
    @Schema(description = "В процессе")
    PROCESSED("PROCESSED", "В процессе", "Задача в процессе выполнения");

    private final String code;
    private final String name;
    private final String description;

    public static TaskStatus fromCode(String code) {

        if (code == null || code.isBlank()) {
            throw new UnknowTaskStatusException("Статус не может быть пустым");
        }

        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new UnknowTaskStatusException("Неизвестный статус: " + code);
    }
}
