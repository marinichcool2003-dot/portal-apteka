package com.apteka.portal.components.validators;

import org.springframework.stereotype.Component;

import com.apteka.portal.exceptions.InvalidPhoneNumberException;

@Component
public class PhoneNumberValidator {
    public String getCleanPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Номер телефона не может быть пустым");
        }
        if (phoneNumber.length() > 20) {
            throw new InvalidPhoneNumberException("Номер телефона не можнт быть более 20 символов!");
        }
        String cleanNumber = phoneNumber.replaceAll("\\D", "");
        if (!cleanNumber.matches("^[78]\\d{10}$")) {
            throw new InvalidPhoneNumberException("Некорректный формат номера. Ожидается 11 цифр.");
        }
        return cleanNumber;
    }

    public String getCleanInternalNumber(String internalNumber) {
        if (internalNumber == null || internalNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Внутренний номер телефона не может быть пустым");
        }
        if (internalNumber.length() > 20) {
            throw new InvalidPhoneNumberException("Внутренний номер телефона не можнт быть более 20 символов!");
        }
        String cleanNumber = internalNumber.replaceAll("\\D", "");
        return cleanNumber;
    }

    public String getCleanExtensionNumber(String extensionNumber) {
        if (extensionNumber == null || extensionNumber.isBlank()) {
            throw new InvalidPhoneNumberException("Добавочный номер телефона не может быть пустым");
        }
        if (extensionNumber.length() > 20) {
            throw new InvalidPhoneNumberException("Добавочный номер телефона не можнт быть более 20 символов!");
        }
        String cleanNumber = extensionNumber.replaceAll("\\D", "");
        return cleanNumber;
    }
}
