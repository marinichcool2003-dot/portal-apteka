package com.apteka.portal.components.validators;

import java.util.Locale;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidLoginException;

@Component
public class LoginValidator {

    // AUDIT-FIX: login — короткий идентификатор без @ (email валидируется отдельно)
    public String getCleanLogin(String login) {
        if (login == null) {
            throw new InvalidLoginException("Логин не может быть пустым!");
        }
        String cleanLogin = login.replaceAll("\\s", "").toLowerCase(Locale.ROOT);

        if (!StringUtils.hasText(cleanLogin)) {
            throw new InvalidLoginException("Логин не может быть пустым!");
        }

        if (cleanLogin.length() < 3) {
            throw new InvalidLoginException("Логин должен быть не короче 3 символов!");
        }

        if (cleanLogin.length() > 50) {
            throw new InvalidLoginException("Логин не может быть более 50 символов!");
        }

        if (!cleanLogin.matches("^[a-zA-Z0-9_.-]+$")) {
            throw new InvalidLoginException(
                    "Логин может содержать только английские буквы, цифры и символы (_ . -)");
        }

        if (cleanLogin.contains("@")) {
            throw new InvalidLoginException("Логин не должен содержать символ @ (используйте поле email)!");
        }

        return cleanLogin;
    }
}
