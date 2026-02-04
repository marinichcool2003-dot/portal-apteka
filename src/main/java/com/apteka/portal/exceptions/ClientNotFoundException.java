package com.apteka.portal.exceptions;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException{
    public ClientNotFoundException(UUID id){
        super("Пользователь с ID: " + id + " не найден!");
    }
    public ClientNotFoundException(String message){
        super(message);
    }
}
