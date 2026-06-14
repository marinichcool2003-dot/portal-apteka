package com.apteka.portal.exceptions;

public class MainPageLinkAlreadyExistsException extends RuntimeException{
    public MainPageLinkAlreadyExistsException(String message) {
        super(message);
    }
}
