package com.apteka.portal.exceptions;

public class DublicateGroupAptekiException extends RuntimeException {
    public DublicateGroupAptekiException(String name) {
        super("Группа аптеки: " + name + " уже существует!");
    }
}
