package com.apteka.portal.models;

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

    public static TaskStatus fromDescription(String description){
        for (TaskStatus status : values()) {
            if (status.description.equals(description)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + description);
    }
}
