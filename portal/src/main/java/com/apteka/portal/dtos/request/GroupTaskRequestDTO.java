package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record GroupTaskRequestDTO(
    @NotBlank(message = "Наименование группы задач не может быть пустым")
    String name,

    @NotNull(message = "Идентификатор группы сотрудников связанной с группой задач не может быть пустым")
    @Positive(message = "Группа пользователя должна быть больше нуля")
    Integer userGroupId
) 
{}
