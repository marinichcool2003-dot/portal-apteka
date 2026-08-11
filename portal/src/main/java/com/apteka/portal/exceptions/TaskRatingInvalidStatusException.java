package com.apteka.portal.exceptions;

public class TaskRatingInvalidStatusException extends RuntimeException {
    public TaskRatingInvalidStatusException(Long taskId) {
        super("Оценку можно выставить только для закрытой или отклонённой задачи (ID: " + taskId + ")!");
    }
}
