package com.apteka.portal.exceptions;

public class DublicateGroupUserException extends RuntimeException{
    public DublicateGroupUserException(String name){
        super("Группа с названием: " + name + " уже существует!");
    }
    public DublicateGroupUserException(){
        super("Группа уже существует!");
    }
}
