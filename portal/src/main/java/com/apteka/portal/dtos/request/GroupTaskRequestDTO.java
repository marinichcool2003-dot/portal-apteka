package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GroupTaskRequestDTO(
    @NotBlank(message = "Наименование группы задач не может быть пустым")
    String name,

    @NotNull(message = "Идентификатор группы сотрудников связанной с группой задач не может быть пустым") 
    Integer userGroupId
) 
{}
