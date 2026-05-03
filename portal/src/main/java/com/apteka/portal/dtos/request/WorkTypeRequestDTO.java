package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WorkTypeRequestDTO(
    @NotBlank(message = "Наименование типа работ не может быть пустым")
    String name,

    @NotNull
    Integer groupTaskId
) {}
