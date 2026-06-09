package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NewsUpdateRequestDTO(
        @NotBlank(message = "Заголовок новости не может быть пустым") @Size(min = 3, max = 50, message = "Заголовок должен содержать от 3 до 50 символов") String title,

        @NotBlank(message = "Текст новости не может быть пустым") @Size(min = 10, max = 2000, message = "Текст новости должен содержать от 3 до 2000 символов")
        String newsText) {

}
