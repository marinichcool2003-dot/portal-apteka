package com.apteka.portal.exceptions;

import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;

public class AptekaNotFoundException extends EntityNotFoundException{
    public AptekaNotFoundException(){
        super("Аптека не найдена!");
    }
    public AptekaNotFoundException(String message){
        super(message);
    }
    public AptekaNotFoundException(UUID id){
        super("Аптека с ID: " + id + " не найдена!");
    }
}
