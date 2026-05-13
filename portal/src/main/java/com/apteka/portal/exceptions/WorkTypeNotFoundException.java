package com.apteka.portal.exceptions;

public class WorkTypeNotFoundException extends RuntimeException{

    public WorkTypeNotFoundException() {
        super("Тип работ не найден");
    }

    public WorkTypeNotFoundException(Integer id) {
        super("Тип работ с ID: " + id + "не найден");
    }
}
