package com.apteka.portal.dtos.request;

import java.util.UUID;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder(toBuilder = true)
@Schema(description = "Фильтры задач подразделения")
public record DepartamentTaskWithFiltersDTO(
        @Schema(description = "Идентификатор группы пользователей")
        @Positive(message = "Группа пользователей должна быть больше нуля") Integer groupId,
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
        @Positive(message = "Идентификатор типа задач должен быть больше нуля")Integer groupTaskId) {
}
