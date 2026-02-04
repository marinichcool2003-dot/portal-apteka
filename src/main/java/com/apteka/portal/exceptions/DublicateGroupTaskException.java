package com.apteka.portal.exceptions;

public class DublicateGroupTaskException extends RuntimeException {
    public DublicateGroupTaskException(String name) {
        super("Тип задачи: " + name + " уже существует!");
    }
}
