package com.apteka.portal.exceptions;

public class InvalidGroupUserException extends RuntimeException{
    public InvalidGroupUserException(String message) {
        super(message);
    }
}
