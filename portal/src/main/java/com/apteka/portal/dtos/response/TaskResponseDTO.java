package com.apteka.portal.dtos.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.apteka.portal.models.Task;

public record TaskResponseDTO(
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
            task.getPriority() != null ? task.getPriority().name() : null,
            Optional.ofNullable(task.getWorkType()).map(wt -> wt.getId()).orElse(null),
            Optional.ofNullable(task.getWorkType()).map(wt -> wt.getName()).orElse(null),
            Optional.ofNullable(task.getWorkType()).map(wt -> wt.getGroupTask()).map(gt -> gt.getName()).orElse(null),
            Optional.ofNullable(task.getWorkType())
                .map(wt -> wt.getGroupTask())
                .map(gt -> gt.getUserGroup())
                .map(ug -> ug.getName())
                .orElse(null),
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
