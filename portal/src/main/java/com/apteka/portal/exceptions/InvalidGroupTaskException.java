package com.apteka.portal.exceptions;

public class InvalidGroupTaskException extends RuntimeException {
    public InvalidGroupTaskException() {
        super("Группа задачи не может быть пустой!");
    }

    public InvalidGroupTaskException(String message) {
        super(message);
    }
}
