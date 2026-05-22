package com.apteka.portal.dtos.request;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;

public record DepartmentFilterRequestDTO(
        TaskStatus status,
        TaskPriority priority,
        @Positive
        Integer workTypeId,
        @Positive
        Integer groupTaskId) {
}
