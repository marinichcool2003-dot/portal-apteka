package com.apteka.portal.exceptions;

public class InvalidClientLoginException extends RuntimeException{
    public InvalidClientLoginException() {
        super("Логин не должен быть пустым!");
    }

    public InvalidClientLoginException(String message){
        super(message);
    }
}
