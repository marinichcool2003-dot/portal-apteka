package com.apteka.portal.dtos.response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserType;

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
        
    public record UserShortInfo(Object id, UserType type, String displayName) {
    }

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
            resolveCreator(task),
            resolveAssignee(task),
            Optional.ofNullable(task.getEmployeeComments()).orElse(Collections.emptySet()).stream()
                .map(TaskCommentResponseDTO::from)
                .toList(),
            Optional.ofNullable(task.getPictures()).orElse(Collections.emptySet()).stream()
                .map(TaskPictureResponseDTO::from)
                .toList()
        );
    }

    private static UserShortInfo resolveCreator(Task task) {
        if (task.getCreatedByClient() != null) {
            return new UserShortInfo(task.getCreatedByClient().getId(), UserType.CLIENT,
                    task.getCreatedByClient().getFullName());
        } else if (task.getCreatedByApteka() != null) {
            String aptekaName = Optional.ofNullable(task.getCreatedByApteka().getUserGroup())
                    .map(ug -> ug.getName() + " " + task.getCreatedByApteka().getNumber())
                    .orElse(task.getCreatedByApteka().getLogin());
            return new UserShortInfo(task.getCreatedByApteka().getId(), UserType.APTEKA, aptekaName);
        }
        return null;
    }

    private static UserShortInfo resolveAssignee(Task task) {
        if (task.getAssignedClient() != null) {
            return new UserShortInfo(task.getAssignedClient().getId(), UserType.CLIENT,
                    task.getAssignedClient().getFullName());
        } else if (task.getAssignedApteka() != null) {
            String aptekaName = Optional.ofNullable(task.getAssignedApteka().getUserGroup())
                    .map(ug -> ug.getName() + " " + task.getAssignedApteka().getNumber())
                    .orElse(task.getAssignedApteka().getLogin());
            return new UserShortInfo(task.getAssignedApteka().getId(), UserType.APTEKA, aptekaName);
        }
        return null;
    }
}
