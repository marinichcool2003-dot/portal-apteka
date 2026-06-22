package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder(toBuilder = true)
public record DepartamentTaskWithFiltersDTO(
        @Positive(message = "Группа пользователей должна быть больше нуля") Integer groupId,
        UUID creatorClientId,
        UUID creatorAptekaId,
        UUID specificClientId,
        UUID specificAptekaId,
        TaskStatus status,
        TaskPriority priority,
        @Positive(message = "Идентификатор вида работ должен быть больше нуля") Integer workTypeId,
        @Positive(message = "Идентификатор типа задач должен быть больше нуля")Integer groupTaskId) {
}
