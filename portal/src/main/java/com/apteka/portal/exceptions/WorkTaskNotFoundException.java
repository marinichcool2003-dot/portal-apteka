package com.apteka.portal.exceptions;

public class WorkTaskNotFoundException extends RuntimeException{

    public WorkTaskNotFoundException() {
        super("Тип работ не найден");
    }

    public WorkTaskNotFoundException(Integer id) {
        super("Тип работ с ID: " + id + "не найден");
    }
}
