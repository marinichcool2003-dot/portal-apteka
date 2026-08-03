package com.apteka.portal.dtos.request;

import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Фильтр задач отдела")
public record DepartmentFilterRequestDTO(
        @Schema(description = "Статус задачи")
        TaskStatus status,
        @Schema(description = "Приоритет задачи")
        TaskPriority priority,
        @Schema(description = "Идентификатор типа работ")
        @Positive(message = "Вид работ должен быть больше нуля")
        Integer workTypeId,
        @Schema(description = "Идентификатор группы задач")
        @Positive(message = "Тип задач должен быть больше нуля")
        Integer groupTaskId) {
}
