package com.apteka.portal.exceptions;

public class AptekaTaskRatingAlreadyExistsException extends RuntimeException {
    public AptekaTaskRatingAlreadyExistsException(Long taskId) {
        super("Оценка для задачи с ID: " + taskId + " уже существует!");
    }
}
