package com.apteka.portal.dtos.response;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.apteka.portal.models.Task;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Полный ответ с данными задачи")
public record TaskResponseDTO(
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
        UserShortInfo assignedBy,
        @Schema(description = "Комментарии к задаче")
        List<TaskCommentResponseDTO> comments,
        @Schema(description = "Изображения задачи")
        List<TaskPictureResponseDTO> pictures) {
        
    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(
            task.getId(),
            task.getTitle(), 
            task.getDescription(),
            task.getCreationDate(),
            task.getUpdatedDate(),
            task.getClosingDate(),
            task.getStatus() != null ? task.getStatus().name() : null,
            Optional.ofNullable(task.getWorkType()).map(WorkTypeResponseDTO::from).orElse(null),
            UserShortInfo.resolveCreator(task),
            UserShortInfo.resolveAssignee(task),
            Optional.ofNullable(task.getEmployeeComments()).orElse(Collections.emptySet()).stream()
                .map(TaskCommentResponseDTO::from)
                .toList(),
            Optional.ofNullable(task.getPictures()).orElse(Collections.emptySet()).stream()
                .map(TaskPictureResponseDTO::from)
                .toList()
        );
    }

}
