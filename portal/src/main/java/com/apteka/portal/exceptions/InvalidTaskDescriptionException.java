package com.apteka.portal.exceptions;

public class InvalidTaskDescriptionException extends RuntimeException {
    public InvalidTaskDescriptionException() {
        super("Описание задачи не может быть пустым!");
    }
}
