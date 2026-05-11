package com.apteka.portal.dtos.request;

import org.springframework.stereotype.Component;

import com.apteka.portal.exceptions.InvalidAptekaPasswordException;
import com.apteka.portal.exceptions.InvalidClientPasswordException;

@Component
public class PasswordValidator {
    public void validatePassword(String password, boolean isClient) {
        if (password == null || password.isBlank()) {
            throw isClient
                ? new InvalidClientPasswordException("Пароль не может быть пустым")
                : new InvalidAptekaPasswordException("Пароль не может быть пустым");
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

        if (!password.matches(passwordPattern)) {
            throw isClient
                ? new InvalidClientPasswordException("Пароль слишком простой")
                : new InvalidAptekaPasswordException("Пароль слишком простой");
        }
    }
}
