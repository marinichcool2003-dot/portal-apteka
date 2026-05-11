package com.apteka.portal.exceptions;

public class InvalidAptekaPhoneNumberException extends RuntimeException{
    public InvalidAptekaPhoneNumberException(String message){
        super(message);
    }
}
