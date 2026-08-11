package com.apteka.portal.exceptions;

public class OtpCooldownException extends RuntimeException {
    public OtpCooldownException() {
        super("Повторная отправка кода возможна через некоторое время");
    }
}
