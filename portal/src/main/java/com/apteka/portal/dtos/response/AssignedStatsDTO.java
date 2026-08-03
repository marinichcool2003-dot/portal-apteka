package com.apteka.portal.dtos.response;

import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Статистика назначенных задач")
public record AssignedStatsDTO(
    @Schema(description = "Идентификатор клиента")
    UUID clientId,
    @Schema(description = "Общее количество задач")
    Long totalCount,
    @Schema(description = "Количество открытых задач")
    Long openCount,
    @Schema(description = "Количество закрытых задач")
    Long closedCount,
    @Schema(description = "Количество отклонённых задач")
    Long deniedCount,
    @Schema(description = "Количество обработанных задач")
    Long processedCount
) {}
