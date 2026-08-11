package com.apteka.portal.report.dtos.response.kpi

import java.util.UUID
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Плоский KPI-отчёт по выполнению задач пользователя")
data class FlatTaskUserCompleteReportDTO (
    @Schema(description = "Идентификатор группы пользователей")
    val groupId: Int,
    @Schema(description = "Наименование группы")
    val groupName: String,

    @Schema(description = "Идентификатор клиента")
    val clientId: UUID,
    @Schema(description = "ФИО клиента")
    val clientFullName: String,

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