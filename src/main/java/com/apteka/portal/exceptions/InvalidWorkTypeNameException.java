package com.apteka.portal.exceptions;

public class InvalidWorkTypeNameException extends RuntimeException{
    public InvalidWorkTypeNameException() {
        super("Тип работ не может быть пустым!");
    }
}
