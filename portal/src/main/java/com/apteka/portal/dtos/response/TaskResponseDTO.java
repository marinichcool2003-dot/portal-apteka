package com.apteka.portal.dtos.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskComments;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        String comments,
        LocalDateTime creationDate,
        LocalDateTime updatedDate,
        LocalDateTime closingDate,
        String status,
        String priority,
        String workType,
        String groupTask,
        String createdByAptekaLogin,
        String createdByClientFullName,
        String assignedAptekaLogin,
        String assignedClientFullName,
        String assignedUserGroup,
        List<TaskComments> Allcomments 
) {
    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getComments(),
                task.getCreationDate(),
                task.getUpdatedDate(),
                task.getClosingDate(),
                task.getStatus().getDescription(),
                task.getPriority().getDescription(),
                Optional.ofNullable(task.getWorkType()).map(WorkType::getName).orElse(null),
                Optional.ofNullable(task.getWorkType().getGroupTask()).map(GroupTask::getName).orElse(null),
                task.getCreatedByApteka() != null ? task.getCreatedByApteka().getLogin() : null,
                task.getCreatedByClient() != null ? task.getCreatedByClient().getFullName() : null,
                task.getAssignedApteka() != null ? task.getAssignedApteka().getLogin() : null,
                task.getAssignedClient() != null ? task.getAssignedClient().getFullName() : null,
                Optional.ofNullable(task.getWorkType().getGroupTask().getUserGroup()).map(UserGroup::getName).orElse(null),
                task.getEmployeeComments()
        );
    }
}