package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidFullNameException;

@Component
public class FullNameValidator {
    public String getCleanFullName(String fullName) {
        if (!StringUtils.hasText(fullName)) {
            throw new InvalidFullNameException("ФИО не может быть пустым");
        }

        String normalizedName = fullName.replaceAll("[\\s_]+", " ").trim();
        
        if (normalizedName.length() > 100) {
            throw new InvalidFullNameException("ФИО не может быть длиннее 100 символов");
        }

        if (!normalizedName.matches("^[\\p{L}'-]+(?:\\s[\\p{L}'-]+){1,2}$")) {
            throw new InvalidFullNameException("Введите корректные Фамилию и Имя (или ФИО)");
        }

        return normalizedName;
    }
}
