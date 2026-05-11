package com.apteka.portal.exceptions;

public class AlreadyHaveThisPasswordException extends RuntimeException{
    public AlreadyHaveThisPasswordException() {
        super("Вы не внесли никаких изменений в свой пароль");
    }
}
