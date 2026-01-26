package com.apteka.portal.exceptions;

public class DublicateAptekaLoginException extends RuntimeException {
    public DublicateAptekaLoginException() {
        super("Аптека с данной почтой уже существует!");
    }

    public DublicateAptekaLoginException(String login) {
        super("Аптека с почтой: " + login + " уже существует!");
    }
}
