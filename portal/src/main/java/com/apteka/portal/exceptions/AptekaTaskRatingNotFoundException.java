package com.apteka.portal.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class AptekaTaskRatingNotFoundException extends EntityNotFoundException {
    public AptekaTaskRatingNotFoundException(Long taskId) {
        super("Оценка для задачи с ID: " + taskId + " не найдена!");
    }
}
