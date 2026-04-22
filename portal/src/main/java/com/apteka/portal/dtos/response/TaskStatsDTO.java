package com.apteka.portal.dtos.response;

public record TaskStatsDTO(
    long totalTasks,
    long newTasks,
    long inProgressTasks,
    long completedTasks
) {}
