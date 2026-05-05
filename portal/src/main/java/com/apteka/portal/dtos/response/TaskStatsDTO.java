package com.apteka.portal.dtos.response;

public record TaskStatsDTO(
    Long totalCount,
    Long openCount,
    Long closedCount,
    Long deniedCount,
    Long processedCount
) {}
