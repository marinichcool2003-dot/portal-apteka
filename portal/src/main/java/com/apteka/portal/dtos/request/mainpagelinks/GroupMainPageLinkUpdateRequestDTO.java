package com.apteka.portal.dtos.request.mainpagelinks;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление группы ссылок главной страницы")
public record GroupMainPageLinkUpdateRequestDTO(
        @Schema(description = "Наименование")
        @NotEmpty(message = "Наименование группы ссылок не может быть пустым!") @Size(max = 50, message = "Наименование группы ссылок не может быть больше 50 символов!") String name,
        @Schema(description = "Описание")
        @Size(max = 100, message = "Описание группы ссылок не может быть больше 100 символов!") String description) {
}
