package com.apteka.portal.exceptions;

public class InvalidClientFullNameException extends RuntimeException{
    public InvalidClientFullNameException(String message) {
        super(message);
    }
}
