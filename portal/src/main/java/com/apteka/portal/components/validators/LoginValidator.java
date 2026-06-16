package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidLoginException;

@Component
public class LoginValidator {
    public String getCleanLogin(String login) {
        if (login == null) {
            throw new InvalidLoginException("Логин не может быть пустым!");
        }
        String cleanLogin = login.replaceAll("\\s", "");

        if (!StringUtils.hasText(cleanLogin)) {
            throw new InvalidLoginException("Логин не может быть пустым!");
        }

        if (cleanLogin.length() > 50) {
            throw new InvalidLoginException("Логин не может быть более 50 символов!");
        }

        if (!cleanLogin.matches("^[a-zA-Z0-9_.-@]+$")) {
            throw new InvalidLoginException("Логин может содержать только английские буквы, цифры и символы (_ . - @)");
        }

        if (!cleanLogin.endsWith("@farmp.ru")) {
            throw new InvalidLoginException("Логин должен содержать домен!");
        }

        return cleanLogin;
    }
}
