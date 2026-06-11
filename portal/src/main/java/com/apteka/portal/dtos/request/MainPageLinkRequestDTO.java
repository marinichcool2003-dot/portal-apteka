package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MainPageLinkRequestDTO(
    @NotBlank(message = "Наименование ссылки не может быть пустым!")
    String name,

    @NotBlank(message = "Ссылка не может быть пустой!")
    @Size(max = 1000, message = "Ссылка не может содержать более 1000 символов!")
    String link
) {}
