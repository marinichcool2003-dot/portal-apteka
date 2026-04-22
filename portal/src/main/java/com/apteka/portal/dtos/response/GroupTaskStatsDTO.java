package com.apteka.portal.dtos.response;

public record GroupTaskStatsDTO(
    Integer groupId,
    String groupName,
    long openTasks,
    long completedTasks,
    long totalTasks
) {}
