package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidLoginException;

@Component
public class LoginValidator {
    public String getCleanLogin(String login) {

        if (!StringUtils.hasText(login)) {
            throw new InvalidLoginException("Логин не может быть пустым!");
        }

        if (login.length() > 50) {
            throw new InvalidLoginException("Логин не может быть более 50 символов!");
        }

        if (!login.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new InvalidLoginException("Логин может содержать только английские буквы, цифры и символы (_ . -)");
        }

        if (!login.endsWith("@farmp.ru")) {
            throw new InvalidLoginException("Логин должен содержать домен!");
        }

        return login.replaceAll("\\s", "");
    }
}
