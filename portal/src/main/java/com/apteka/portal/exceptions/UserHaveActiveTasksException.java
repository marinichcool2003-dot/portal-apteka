package com.apteka.portal.exceptions;

import java.util.UUID;

public class UserHaveActiveTasksException extends RuntimeException{
    public UserHaveActiveTasksException(UUID id) {
        super("У пользователя имеются активные задачи!");
    }
}
