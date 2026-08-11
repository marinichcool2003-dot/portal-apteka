package com.apteka.portal.report.dtos.response.aptekaproblems

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Отчёт по основным проблемам конкретной аптеки")
data class TaskAptekaReportDTO(
    @Schema(description = "Идентификатор аптеки")
    val aptekaId: UUID,
    @Schema(description = "Наименование аптеки")
    val aptekaName: String,

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
    @Schema(description = "Доля задач аптеки от всех задач периода, %")
    val percentAll: Double,

    @Schema(description = "Категории задач (group_task)")
    val groupTasks: List<TaskAptekaGroupTaskReportDTO>
)
