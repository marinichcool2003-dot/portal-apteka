package com.apteka.portal.exceptions;

public class UnknowTaskStatusException extends RuntimeException{
    public UnknowTaskStatusException(String message) {
        super(message);
    }
}
