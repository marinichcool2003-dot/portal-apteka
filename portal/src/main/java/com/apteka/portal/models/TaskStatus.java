package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowTaskStatusException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TaskStatus {
    OPEN("Открыта"),
    CLOSED("Закрыта"),
    DENIED("Отклонена"),
    PROCESSED("В процессе");

    private final String description;

    public static TaskStatus fromDescription(String description) {

        if (description == null || description.isBlank()) {
            throw new UnknowTaskStatusException("Статус не может быть пустым");
        }

        for (TaskStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        throw new UnknowTaskStatusException("Неизвестный статус: " + description);
    }
}
