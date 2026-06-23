package com.apteka.portal.dtos.response;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.apteka.portal.models.Task;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        Instant creationDate,
        Instant updatedDate,
        Instant closingDate,
        String status,

        WorkTypeResponseDTO workType,

        UserShortInfo createdBy,
        UserShortInfo assignedBy,

        List<TaskCommentResponseDTO> comments,
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
