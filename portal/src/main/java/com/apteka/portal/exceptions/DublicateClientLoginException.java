package com.apteka.portal.exceptions;

public class DublicateClientLoginException extends RuntimeException{
    public DublicateClientLoginException(String message) {
        super(message);
    }
}
