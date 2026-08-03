package com.apteka.portal.report.dtos.request

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Запрос отчёта за период по конкретной группе пользователей")
data class ReportTaskByGroupRequestDTO(
    @Schema(description = "Идентификатор группы пользователей", example = "1")
    val groupId: Int,
    @Schema(description = "Начало периода (UTC)")
    override val startDate: Instant,
    @Schema(description = "Конец периода (UTC)")
    override val endDate: Instant
) : ReportTimeRequestDTO(startDate, endDate)
