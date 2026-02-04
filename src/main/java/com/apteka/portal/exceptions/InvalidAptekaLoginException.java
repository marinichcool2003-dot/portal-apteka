package com.apteka.portal.exceptions;

public class InvalidAptekaLoginException extends RuntimeException {
    public InvalidAptekaLoginException() {
        super("Логин аптеки не может быть пустым!");
    }

    public InvalidAptekaLoginException(String message) {
        super(message);
    }
}
