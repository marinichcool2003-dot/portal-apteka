package com.apteka.portal.dtos.response;

import java.time.Instant;
import java.util.Optional;

import com.apteka.portal.models.Task;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткий ответ с данными задачи")
public record TaskShortResponseDTO(
        @Schema(description = "Идентификатор")
        Long id,
        @Schema(description = "Заголовок")
        String title,
        @Schema(description = "Описание")
        String description,
        @Schema(description = "Дата создания")
        Instant creationDate,
        @Schema(description = "Дата обновления")
        Instant updatedDate,
        @Schema(description = "Дата закрытия")
        Instant closingDate,
        @Schema(description = "Статус задачи")
        String status,
        @Schema(description = "Тип работ")
        WorkTypeResponseDTO workType,
        @Schema(description = "Создатель задачи")
        UserShortInfo createdBy,
        @Schema(description = "Исполнитель задачи")
        UserShortInfo assignedBy) {
    public static TaskShortResponseDTO from(Task task) {
        return new TaskShortResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getCreationDate(),
                task.getUpdatedDate(),
                task.getClosingDate(),
                task.getStatus() != null ? task.getStatus().name() : null,
                Optional.ofNullable(task.getWorkType()).map(WorkTypeResponseDTO::from).orElse(null),
                UserShortInfo.resolveCreator(task),
                UserShortInfo.resolveAssignee(task));
    }
}
