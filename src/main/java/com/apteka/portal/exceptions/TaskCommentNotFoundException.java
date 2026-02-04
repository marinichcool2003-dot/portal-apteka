package com.apteka.portal.exceptions;

public class TaskCommentNotFoundException extends RuntimeException{
    public TaskCommentNotFoundException(Long id){
        super("Комментарий с ID: " + id + " не найден!");
    }

    public TaskCommentNotFoundException(){
        super("Комментарий не найден!");
    }
}
