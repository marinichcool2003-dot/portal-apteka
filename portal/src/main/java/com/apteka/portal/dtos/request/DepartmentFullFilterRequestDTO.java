package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;

public record DepartmentFullFilterRequestDTO(
        @Positive Integer groupId,
        UUID creatorClientId,
        @Positive Integer creatorAptekaId,
        UUID specificClientId,
        @Positive Integer specificAptekaId,
        TaskStatus status,
        TaskPriority priority,
        @Positive Integer workTypeId,
        @Positive Integer groupTaskId) {

}
