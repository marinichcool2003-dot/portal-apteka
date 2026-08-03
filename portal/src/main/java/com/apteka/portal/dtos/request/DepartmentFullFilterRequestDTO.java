package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Полный фильтр задач отдела")
public record DepartmentFullFilterRequestDTO(
        @Schema(description = "Идентификатор группы пользователей")
        @Positive(message = "Идентификатор группы пользователя должен быть больше нуля") Integer groupId,
        @Schema(description = "Идентификатор создателя задачи")
        UUID creatorId,
        @Schema(description = "Идентификатор исполнителя задачи")
        UUID assignerId,
        @Schema(description = "Статус задачи")
        TaskStatus status,
        @Schema(description = "Приоритет задачи")
        TaskPriority priority,
        @Schema(description = "Идентификатор типа работ")
        @Positive(message = "Идентификатор вида работ должен быть больше нуля") Integer workTypeId,
        @Schema(description = "Идентификатор группы задач")
        @Positive(message = "идентификатор типа задач должен быть больше нуля") Integer groupTaskId) {

}
