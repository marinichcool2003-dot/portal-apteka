package com.apteka.portal.exceptions;

import jakarta.persistence.EntityNotFoundException;

public class GroupAptekiNotFoundException extends EntityNotFoundException {
    public GroupAptekiNotFoundException() {
        super("Группа не найдена!");
    }

    public GroupAptekiNotFoundException(String message) {
        super(message);
    }

    public GroupAptekiNotFoundException(Integer id) {
        super("Группа с ID: " + id + " не найдена!");
    }
}
