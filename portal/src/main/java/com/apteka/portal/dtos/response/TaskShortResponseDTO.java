package com.apteka.portal.dtos.response;

import java.time.LocalDateTime;
import java.util.Optional;

import com.apteka.portal.models.Task;

public record TaskShortResponseDTO(
        Long id,
        String title,
        String description,
        LocalDateTime creationDate,
        LocalDateTime updatedDate,
        LocalDateTime closingDate,
        String status,
        String priority,

        Integer workTypeId,
        String workTypeName,
        String groupTaskName,
        String userGroupName,
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
                task.getPriority() != null ? task.getPriority().name() : null,
                Optional.ofNullable(task.getWorkType()).map(wt -> wt.getId()).orElse(null),
                Optional.ofNullable(task.getWorkType()).map(wt -> wt.getName()).orElse(null),
                Optional.ofNullable(task.getWorkType()).map(wt -> wt.getGroupTask()).map(gt -> gt.getName())
                        .orElse(null),
                Optional.ofNullable(task.getWorkType())
                        .map(wt -> wt.getGroupTask())
                        .map(gt -> gt.getUserGroup())
                        .map(ug -> ug.getName())
                        .orElse(null),
                UserShortInfo.resolveCreator(task),
                UserShortInfo.resolveAssignee(task));
    }
}
