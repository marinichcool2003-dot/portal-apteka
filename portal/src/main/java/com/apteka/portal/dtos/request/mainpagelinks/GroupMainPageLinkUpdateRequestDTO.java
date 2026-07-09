package com.apteka.portal.dtos.request.mainpagelinks;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record GroupMainPageLinkUpdateRequestDTO(
        @NotEmpty(message = "Наименование группы ссылок не может быть пустым!") @Size(max = 50, message = "Наименование группы ссылок не может быть больше 50 символов!") String name,

        @Size(max = 100, message = "Описание группы ссылок не может быть больше 100 символов!") String description) {
}
