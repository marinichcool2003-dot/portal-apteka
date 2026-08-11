package com.apteka.portal.dtos.request.mainpagelinks;

import org.hibernate.validator.constraints.URL;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление ссылки главной страницы")
public record MainPageLinkUpdateRequestDTO(
        @Schema(description = "Наименование")
        @Size(max = 50, message = "Наименование ссылки не должно превышать 50 символов!")
        String name,
        @Schema(description = "URL-ссылка")
        @Size(max = 1000, message = "Ссылка не может содержать более 1000 символов!")
        @URL(protocol = "https", message = "Некорректный формат ссылки!")
        String link,
        @Schema(description = "Идентификатор группы ссылок главной страницы")
        @Positive(message = "Идентификатор группы ссылок может быть только больше нуля!")
        Integer groupMainPageLinkId
) {}
