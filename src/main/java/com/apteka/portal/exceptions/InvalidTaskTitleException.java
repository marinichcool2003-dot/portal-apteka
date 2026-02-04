package com.apteka.portal.exceptions;

public class InvalidTaskTitleException extends RuntimeException {
    public InvalidTaskTitleException() {
        super("Заголовок не может быть пустым!");
    }
}
