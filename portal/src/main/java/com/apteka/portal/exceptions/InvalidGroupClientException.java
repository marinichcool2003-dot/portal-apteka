package com.apteka.portal.exceptions;

public class InvalidGroupClientException extends RuntimeException{
    public InvalidGroupClientException(){
        super("Группа сотрудников не может быть пустой!");
    }
}
