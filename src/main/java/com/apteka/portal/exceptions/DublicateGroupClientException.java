package com.apteka.portal.exceptions;

public class DublicateGroupClientException extends RuntimeException{
    public DublicateGroupClientException(String name){
        super("Группа с названием: " + name + " уже существует!");
    }
    public DublicateGroupClientException(){
        super("Группа уже существует!");
    }
}
