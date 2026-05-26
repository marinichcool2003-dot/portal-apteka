package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record WorkTypeRequestDTO(
    @NotBlank(message = "Наименование типа работ не может быть пустым")
    String name,

    @NotNull(message = "Тип работ не может быть null")
    @Positive(message = "Тип работ должен быть больше нуля")
    Integer groupTaskId
) {}
