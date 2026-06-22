package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record WorkTypeUpdateRequestDTO(
        String name,

        @Positive(message = "Тип работ должен быть больше нуля!") Integer groupTaskId,

        String priorityCode,

        @Size(max = 2048, message = "Ссылка слишком длинная!") String wiki_link) {

}
