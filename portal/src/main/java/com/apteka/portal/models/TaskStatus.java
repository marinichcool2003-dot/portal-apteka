package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskStatusException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskStatus {
    OPEN("OPEN", "Открыта"),
    CLOSED("CLOSED", "Закрыта"),
    DENIED("DENIED", "Отклонена"),
    PROCESSED("PROCESSED", "В процессе");

    private final String code;
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
