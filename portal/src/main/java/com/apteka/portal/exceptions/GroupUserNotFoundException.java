package com.apteka.portal.exceptions;

public class GroupUserNotFoundException extends RuntimeException{
    public GroupUserNotFoundException(Integer id){
        super("Группа с ID " + id + " не найдена!");
    }
    public GroupUserNotFoundException(){
        super("Группа с ID не найдена!");
    }
}
