package com.apteka.portal.exceptions;

public class SelfDeleteException extends RuntimeException{
    public SelfDeleteException(String message) {
        super(message);
    }
}
