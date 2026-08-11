package com.apteka.portal.report.dtos.response.aptekaproblems

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Агрегация задач аптеки по виду работ")
data class TaskAptekaWorkTypeReportDTO(
    @Schema(description = "Идентификатор вида работ")
    val workTypeId: Int,
    @Schema(description = "Наименование вида работ")
    val workTypeName: String,

    @Schema(description = "Всего задач")
    val totalTasks: Long,
    @Schema(description = "Выполненные задачи")
    val completedTasks: Long,
    @Schema(description = "Отклонённые задачи")
    val deniedTasks: Long,
    @Schema(description = "Процент выполненных")
    val percentCompletedTasks: Double,
    @Schema(description = "Процент отклонённых")
    val percentDeniedTasks: Double,
    @Schema(description = "Доля от всех задач периода, %")
    val percentAll: Double
)
