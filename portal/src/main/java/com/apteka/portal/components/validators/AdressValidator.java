package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.apteka.portal.exceptions.InvalidAdressException;

@Component
public class AdressValidator {
    public String getCleanAdress(String adress) {
        if (!StringUtils.hasText(adress)) {
            throw new InvalidAdressException("Адрес не может быть пустым");
        }
        String cleanAdress = adress.replaceAll("(?<=\\S)\\s+(?=\\S)", " ").strip();
        if (adress.length() > 255) {
            throw new InvalidAdressException("Адрес не может быть длиннее 255 символов"); 
        }
        if (!cleanAdress.matches(".*[a-zA-Zа-яА-Я].*") || !cleanAdress.matches(".*[0-9].*")) {
            throw new InvalidAdressException("Адрес должен содержать название улицы и номер дома");
        }
        return cleanAdress;
    }
}
