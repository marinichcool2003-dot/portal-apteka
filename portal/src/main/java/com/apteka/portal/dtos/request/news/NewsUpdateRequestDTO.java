package com.apteka.portal.dtos.request.news;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление новости")
public record NewsUpdateRequestDTO(
        @Schema(description = "Заголовок")
        @NotBlank(message = "Заголовок новости не может быть пустым") @Size(min = 3, max = 50, message = "Заголовок должен содержать от 3 до 50 символов") String title,
        @Schema(description = "Текст новости")
        @NotBlank(message = "Текст новости не может быть пустым") @Size(min = 10, max = 2000, message = "Текст новости должен содержать от 3 до 2000 символов")
        String newsText) {

}
