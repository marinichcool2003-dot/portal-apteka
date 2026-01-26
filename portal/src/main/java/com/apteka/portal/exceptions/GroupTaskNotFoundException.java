package com.apteka.portal.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class GroupTaskNotFoundException extends EntityNotFoundException {
    public GroupTaskNotFoundException(Integer id) {
        super("Группа задачи с ID: " + id + " не найдена!");
    }

    public GroupTaskNotFoundException() {
        super("Группа задачи не найдена!");
    }
}
