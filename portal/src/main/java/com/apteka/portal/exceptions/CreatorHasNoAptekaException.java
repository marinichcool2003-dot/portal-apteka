package com.apteka.portal.exceptions;

public class CreatorHasNoAptekaException extends RuntimeException {
    public CreatorHasNoAptekaException(Long taskId) {
        super("Создатель задачи с ID: " + taskId + " не привязан к аптеке!");
    }
}
