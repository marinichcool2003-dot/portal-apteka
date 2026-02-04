package com.apteka.portal.exceptions;

public class InvalidAptekaNumberException extends RuntimeException{
    public InvalidAptekaNumberException(){
        super("Номер аптеки не должен быть пустым!");
    }
    public InvalidAptekaNumberException(String message){
        super(message);
    }
}
