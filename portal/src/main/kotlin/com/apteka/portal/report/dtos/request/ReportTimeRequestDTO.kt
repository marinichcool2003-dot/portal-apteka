package com.apteka.portal.report.dtos.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Запрос отчёта за период времени")
open class ReportTimeRequestDTO(
    @Schema(description = "Начало периода (UTC)", example = "2024-01-01T00:00:00Z")
    open val startDate: Instant,
    @Schema(description = "Конец периода (UTC)", example = "2024-12-31T23:59:59Z")
    open val endDate: Instant
)
