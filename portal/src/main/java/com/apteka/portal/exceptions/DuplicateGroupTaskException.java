package com.apteka.portal.exceptions;

public class DuplicateGroupTaskException extends RuntimeException {
    public DuplicateGroupTaskException(String name) {
        super("Тип задачи: " + name + " уже существует!");
    }
}
