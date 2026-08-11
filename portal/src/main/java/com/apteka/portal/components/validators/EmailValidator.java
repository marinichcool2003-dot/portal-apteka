package com.apteka.portal.components.validators;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidEmailException;

@Component
public class EmailValidator {

    private static final String CORPORATE_DOMAIN = "@farmp.ru";

    // AUDIT-FIX: email — корпоративный адрес @farmp.ru (раньше эти правила были на login)
    public String getCleanEmail(String email) {
        if (email == null) {
            throw new InvalidEmailException("Email не может быть пустым!");
        }
        String cleanEmail = email.replaceAll("\\s", "").toLowerCase(Locale.ROOT);

        if (!StringUtils.hasText(cleanEmail)) {
            throw new InvalidEmailException("Email не может быть пустым!");
        }

        if (cleanEmail.length() > 255) {
            throw new InvalidEmailException("Email не может быть более 255 символов!");
        }

        if (!cleanEmail.matches("^[a-zA-Z0-9_.-@]+$")) {
            throw new InvalidEmailException(
                    "Email может содержать только английские буквы, цифры и символы (_ . - @)");
        }

        if (!cleanEmail.endsWith(CORPORATE_DOMAIN)) {
            throw new InvalidEmailException("Email должен быть в домене @farmp.ru!");
        }

        return cleanEmail;
    }
}
