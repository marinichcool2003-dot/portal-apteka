package com.apteka.portal.exceptions;

public class InvalidOtpException extends RuntimeException {
    public InvalidOtpException() {
        super("Неверный или просроченный код подтверждения");
    }
}
