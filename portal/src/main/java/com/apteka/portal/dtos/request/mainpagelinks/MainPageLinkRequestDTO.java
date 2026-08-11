package com.apteka.portal.dtos.request.mainpagelinks;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание ссылки главной страницы")
public record MainPageLinkRequestDTO(
        @Schema(description = "Наименование")
        @NotBlank(message = "Наименование ссылки не может быть пустым!")
        @Size(max = 50, message = "Наименование ссылки не должно превышать 50 символов!")
        String name,
        @Schema(description = "URL-ссылка")
        @NotBlank(message = "Ссылка не может быть пустой!")
        @Size(max = 1000, message = "Ссылка не может содержать более 1000 символов!")
        @URL(protocol = "https", message = "Некорректный формат ссылки!")
        String link,
        @Schema(description = "Идентификатор группы ссылок главной страницы")
        @NotNull(message = "Идентификатор группы ссылок может быть пустым!")
        @Positive(message = "Идентификатор группы ссылок может быть только больше нуля!")
        Integer groupMainPageLinkId
) {}
 