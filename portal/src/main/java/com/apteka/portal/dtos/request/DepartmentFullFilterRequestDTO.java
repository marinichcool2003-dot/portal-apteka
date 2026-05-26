package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;

public record DepartmentFullFilterRequestDTO(
        @Positive(message = "Идентификатор группы пользователя должен быть больше нуля") Integer groupId,
        UUID creatorClientId,
        @Positive(message = "Идентификатор аптек должен быть больше нуля") Integer creatorAptekaId,
        UUID specificClientId,
        @Positive(message = "Идентификатор аптек должен быть больше нуля") Integer specificAptekaId,
        TaskStatus status,
        TaskPriority priority,
        @Positive(message = "Идентификатор вида работ должен быть больше нуля") Integer workTypeId,
        @Positive(message = "идентификатор типа задач должен быть больше нуля") Integer groupTaskId) {

}
