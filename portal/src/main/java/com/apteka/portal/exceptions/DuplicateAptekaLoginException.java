package com.apteka.portal.exceptions;

public class DuplicateAptekaLoginException extends RuntimeException {
    public DuplicateAptekaLoginException() {
        super("Аптека с данной почтой уже существует!");
    }

    public DuplicateAptekaLoginException(String login) {
        super("Аптека с почтой: " + login + " уже существует!");
    }
}
