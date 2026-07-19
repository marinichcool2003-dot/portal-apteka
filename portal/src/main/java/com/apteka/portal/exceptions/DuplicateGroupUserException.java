package com.apteka.portal.exceptions;

public class DuplicateGroupUserException extends RuntimeException{
    public DuplicateGroupUserException(String name){
        super("Группа с названием: " + name + " уже существует!");
    }
    public DuplicateGroupUserException(){
        super("Группа уже существует!");
    }
}
