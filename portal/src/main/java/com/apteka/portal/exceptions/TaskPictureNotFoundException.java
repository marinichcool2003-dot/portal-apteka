package com.apteka.portal.exceptions;

public class TaskPictureNotFoundException extends RuntimeException{
    public TaskPictureNotFoundException(Long id) {
        super("Картинка приложенная к задаче с " + id + " не найдена!");
    }
}
