package com.apteka.portal.exceptions;

public class OtpMaxAttemptsExceededException extends RuntimeException {
    public OtpMaxAttemptsExceededException() {
        super("Превышено максимальное число попыток ввода кода");
    }
}
