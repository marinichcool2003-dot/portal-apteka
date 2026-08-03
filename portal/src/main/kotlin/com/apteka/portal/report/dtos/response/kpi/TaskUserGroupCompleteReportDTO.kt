package com.apteka.portal.report.dtos.response.kpi

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "KPI-отчёт по выполнению задач группы пользователей")
data class TaskUserGroupCompleteReportDTO(
    @Schema(description = "Общее количество задач")
    val totalTask: Long,
    @Schema(description = "Количество завершённых задач")
    val completedTask: Long,
    @Schema(description = "Количество отклонённых задач")
    val deniedTask: Long,
    @Schema(description = "Процент закрытых задач")
    val closedPercentage: Double,
    @Schema(description = "Процент отклонённых задач")
    val deniedPercentage: Double,
    @Schema(description = "Процент от всех задач")
    val percentageAllTask: Double
)