package com.apteka.portal.dtos.response;

public record TaskStatsDTO(
    AssignedStatsDTO assignedStats,
    CreatedStatsDTO createdStats
) {
    
}
