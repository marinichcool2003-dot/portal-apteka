package com.apteka.portal.exceptions;

public class AptekaCreateException extends RuntimeException {
    public AptekaCreateException(Throwable cause) {
        super("Ошибка при создании аптеки: " + cause.getMessage());
    }

    public AptekaCreateException(String message, Throwable cause) {
        super(message, cause);
    }
}
