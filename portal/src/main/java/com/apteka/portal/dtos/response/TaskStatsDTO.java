package com.apteka.portal.dtos.response;

import java.util.UUID;

public record TaskStatsDTO(
    UUID clientId,
    Long totalCount,
    Long openCount,
    Long closedCount,
    Long deniedCount,
    Long processedCount
) {}
