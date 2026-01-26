package com.apteka.portal.exceptions;

public class InvalidTaskCommentException extends RuntimeException{
    public InvalidTaskCommentException(){
        super("Комментарий не может быть пустым!");
    }
}
