package com.apteka.portal.dtos.response;

public record DepartmentTaskStatsDTO(
    Integer groupId,
    String groupName,
    long openTasks,
    long completedTasks,
    long deniedTasks,
    long totalTasks
) {}
