package com.apteka.portal.exceptions;

public class InvalidRefreshTokenException extends RuntimeException{
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
