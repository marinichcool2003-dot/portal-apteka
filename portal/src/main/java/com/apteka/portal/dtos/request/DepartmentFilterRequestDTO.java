package com.apteka.portal.dtos.request;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;

public record DepartmentFilterRequestDTO(
        TaskStatus status,
        TaskPriority priority,
        @Positive(message = "Вид работ должен быть больше нуля")
        Integer workTypeId,
        @Positive(message = "Тип задач должен быть больше нуля")
        Integer groupTaskId) {
}
