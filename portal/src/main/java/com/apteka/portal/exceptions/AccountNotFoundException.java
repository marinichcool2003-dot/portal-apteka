package com.apteka.portal.exceptions;

import java.util.UUID;

public class AccountNotFoundException extends RuntimeException{
    public AccountNotFoundException(UUID id) {
        super("Аккаунт с ID " + id + "не найден");
    }
}
