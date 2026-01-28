package com.apteka.portal.dtos.response;

import java.util.Date;

import com.apteka.portal.models.Task;

public record TaskResponseDTO(
        Long id,
        String title,
        String description,
        String comments,
        Date date,
        String status,
        String priority,
        String workTask,
        String group,
        String aptekaLogin,
        String clientFullName
) {
    public static TaskResponseDTO from(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getComments(),
                task.getDate(),
                task.getStatus().getDescription(),
                task.getPriority().getDescription(),
                task.getWorkTask().getName(),
                task.getWorkTask().getGroupTask().getName(),
                task.getApteka() != null ? task.getApteka().getLogin() : null,
                task.getClient() != null ? task.getClient().getFullName() : null
        );
    }
}