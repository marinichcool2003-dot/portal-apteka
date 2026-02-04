package com.apteka.portal.exceptions;

public class InvalidGroupAptekiException extends RuntimeException {
    public InvalidGroupAptekiException() {
        super("Группа аптеки не может быть пустой!");
    }

    public InvalidGroupAptekiException(String message) {
        super(message);
    }
}
