package com.apteka.portal.dtos.response;

import java.time.Instant;
import java.util.Optional;

import com.apteka.portal.models.Task;

public record TaskShortResponseDTO(
        Long id,
        String title,
        String description,
        Instant creationDate,
        Instant updatedDate,
        Instant closingDate,
        String status,

        WorkTypeResponseDTO workType,
        UserShortInfo createdBy,
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
