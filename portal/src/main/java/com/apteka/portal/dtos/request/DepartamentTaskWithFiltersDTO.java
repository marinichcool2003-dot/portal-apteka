package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

public record DepartamentTaskWithFiltersDTO(
        Integer groupId,
        UUID creatorClientId,
        Integer creatorAptekaId,
        UUID specificClientId,
        Integer specificAptekaId,
        TaskStatus status,
        TaskPriority priority,
        Integer groupTaskId

) {}
