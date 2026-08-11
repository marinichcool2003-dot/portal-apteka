package com.apteka.portal.report.dtos.response.aptekaproblems

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Агрегация задач аптеки по категории (group_task)")
data class TaskAptekaGroupTaskReportDTO(
    @Schema(description = "Идентификатор категории задач")
    val groupTaskId: Int,
    @Schema(description = "Наименование категории задач")
    val groupTaskName: String,

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
    val percentAll: Double,

    @Schema(description = "Виды работ внутри категории")
    val workTypes: List<TaskAptekaWorkTypeReportDTO>
)
