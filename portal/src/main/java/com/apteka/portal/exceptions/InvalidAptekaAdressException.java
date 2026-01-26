package com.apteka.portal.exceptions;

public class InvalidAptekaAdressException extends RuntimeException{
    public InvalidAptekaAdressException(){
        super("Адрес аптекит не может быть пустым!");
    }
}
