package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AptekaUpdateRequestDTO(

        @Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен") String login,

        @Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,

        @Positive Integer number,

        String adress,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789") String phoneNumber,

        @Positive(message = "Группа аптеки должна быть больше нуля") Integer groupId) {
}
