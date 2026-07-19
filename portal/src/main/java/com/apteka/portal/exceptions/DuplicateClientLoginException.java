package com.apteka.portal.exceptions;

public class DuplicateClientLoginException extends RuntimeException{
    public DuplicateClientLoginException(String message) {
        super(message);
    }
}
