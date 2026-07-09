package com.apteka.portal.dtos.request.mainpagelinks;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record MainPageLinkUpdateRequestDTO(
    @Size(max = 50, message = "Наименование ссылки не должно превышать 50 символов!")
    String name,

    @Size(max = 1000, message = "Ссылка не может содержать более 1000 символов!")
    String link,

    @Positive(message = "Идентификатор группы ссылок может быть только больше нуля!")
    Integer groupMainPageLinkId
) {}
