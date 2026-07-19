package com.apteka.portal.exceptions;

public class DuplicateWorkTypeNameException extends RuntimeException {
    public DuplicateWorkTypeNameException(String name) {
        super("Тип работ под названием: " + name + " уже существует!");
    }
}