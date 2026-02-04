package com.apteka.portal.exceptions;

public class InvalidClientPasswordException extends RuntimeException {
    public InvalidClientPasswordException() {
        super("Пароль не должен быть пустым!");
    }

    public InvalidClientPasswordException(String message){
        super(message);
    }
}
