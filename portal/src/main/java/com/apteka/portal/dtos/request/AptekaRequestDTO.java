package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AptekaRequestDTO(

        @NotBlank(message = "Логин не может быть пустым") @Pattern(regexp = ".*@farmp.ru$", message = "Логин должен содержать домен") String login,

        @NotBlank(message = "Пароль обязателен!") @Size(min = 8, message = "Пароль должен быть минимум 8 символов") @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") String password,

        @NotNull(message = "Номер аптеки обязателен к заполнению") @Positive(message = "Номер аптеки должен быть больше нуля")Integer number,

        String adress,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789") String phoneNumber,

        @NotNull(message = "Группа аптеки не может быть пустой") @Positive(message = "Группа аптеки должна быть больше нуля") Integer groupId) {
}
