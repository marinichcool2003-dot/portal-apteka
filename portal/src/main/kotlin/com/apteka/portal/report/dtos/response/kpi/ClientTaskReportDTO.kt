package com.apteka.portal.report.dtos.response.kpi

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "KPI-отчёт по задачам клиента")
data class ClientTaskReportDTO(
    @Schema(description = "Общее количество задач")
    val totalTask: Long,
    @Schema(description = "Количество завершённых задач")
    val completedTask: Long,
    @Schema(description = "Количество отклонённых задач")
    val deniedTask: Long,
    @Schema(description = "Процент закрытых и отклонённых задач")
    val percentageClosedDenied: Double,
    @Schema(description = "Процент задач группы")
    val percentageGroupTask: Double
)
