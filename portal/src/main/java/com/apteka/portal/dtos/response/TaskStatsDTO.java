package com.apteka.portal.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Сводная статистика задач")
public record TaskStatsDTO(
    @Schema(description = "Статистика назначенных задач")
    AssignedStatsDTO assignedStats,
    @Schema(description = "Статистика созданных задач")
    CreatedStatsDTO createdStats
) {
    
}
