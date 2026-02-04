package com.apteka.portal.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class TaskNotFoundException extends EntityNotFoundException {
    public TaskNotFoundException(Long id) {
        super("Задача с ID: " + id + " не найдена!");
    }

    public TaskNotFoundException(String message) {
        super(message);
    }
}
