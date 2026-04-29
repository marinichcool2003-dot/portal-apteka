package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AptekaRequestDTO(
        String login,

        @NotBlank(message = "Пароль обязателен!") 
        @Size(min = 8, message = "Пароль должен быть минимум 8 символов") 
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", 
            message = "Пароль должен содержать хотя бы одну цифру, одну заглавную букву и один спецсимвол") 
        String password,

        Integer number,

        @NotBlank String adress,

        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789") String phoneNumber,

        @NotNull Integer groupId) {
}
