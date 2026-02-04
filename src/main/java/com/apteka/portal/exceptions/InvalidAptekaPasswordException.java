package com.apteka.portal.exceptions;

public class InvalidAptekaPasswordException extends RuntimeException {
    public InvalidAptekaPasswordException() {
        super("Пароль аптеки не может быть пустым!");
    }

    public InvalidAptekaPasswordException(String message) {
        super(message);
    }
}
