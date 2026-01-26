package com.apteka.portal.exceptions;

public class GroupClientNotFoundException extends RuntimeException{
    public GroupClientNotFoundException(Integer id){
        super("Группа с ID " + id + " не найдена!");
    }
    public GroupClientNotFoundException(){
        super("Группа с ID не найдена!");
    }
}
