package com.apteka.portal.dtos.request.grouptask;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GroupTaskRequestDTO(
    @NotBlank(message = "Наименование группы задач не может быть пустым!")
    String name,

    @NotNull(message = "Идентификатор группы создателя не может быть пустым!")
    @Positive(message = "Группа создателя должна быть больше нуля!")
    Integer creatorGroupId,

    @NotNull(message = "Идентификатор группы создателя не может быть пустым")
    @Positive(message = "Группа создателя должна быть больше нуля!")
    Integer executorGroupId
) 
{}
