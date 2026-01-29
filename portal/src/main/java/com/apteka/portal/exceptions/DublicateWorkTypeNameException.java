package com.apteka.portal.exceptions;

public class DublicateWorkTypeNameException extends RuntimeException {
    public DublicateWorkTypeNameException(String name) {
        super("Тип работ под названием: " + name + " уже существует!");
    }
}