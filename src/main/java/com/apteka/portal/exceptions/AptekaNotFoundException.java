package com.apteka.portal.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class AptekaNotFoundException extends EntityNotFoundException{
    public AptekaNotFoundException(){
        super("Аптека не найдена!");
    }
    public AptekaNotFoundException(String message){
        super(message);
    }
    public AptekaNotFoundException(Integer id){
        super("Аптека с ID: " + id + " не найдена!");
    }
}
