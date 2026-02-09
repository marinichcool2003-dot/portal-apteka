package com.apteka.portal.dtos.response;

import java.util.Date;

import com.apteka.portal.models.Task;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        String comments,
        Date creationDate,
        Date updatedDate,
        Date closingDate,
        String status,
        String priority,
        String workType,
        String groupTask,
        String createdByAptekaLogin,
        String createdByClientFullName,
        String assignedAptekaLogin,
        String assignedClientFullName,
        String groupClient,
        String groupApteki
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
                task.getWorkType().getName(),
                task.getWorkType().getGroupTask().getName(),
                task.getCreatedByApteka() != null ? task.getCreatedByApteka().getLogin() : null,
                task.getCreatedByClient() != null ? task.getCreatedByClient().getFullName() : null,
                task.getAssignedApteka() != null ? task.getAssignedApteka().getLogin() : null,
                task.getAssignedClient() != null ? task.getAssignedClient().getFullName() : null,
                task.getAssignedGroupClient() != null ? task.getAssignedGroupClient().getName() : null,
                task.getAssignedGroupApteki() != null ? task.getAssignedGroupApteki().getName() : null
        );
    }
}