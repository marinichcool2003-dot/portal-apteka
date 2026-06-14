package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;

import com.apteka.portal.exceptions.InvalidPhoneNumberException;

@Component
public class PhoneNumberValidator {
    public String getCleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Номер телефона не может быть пустым");
        }
        String cleanNumber = phoneNumber.replaceAll("\\D", "");
        if (!cleanNumber.matches("^[78]\\d{10}$")) {
            throw new InvalidPhoneNumberException("Некорректный формат номера. Ожидается 11 цифр.");
        }
        return cleanNumber;
    }
}
