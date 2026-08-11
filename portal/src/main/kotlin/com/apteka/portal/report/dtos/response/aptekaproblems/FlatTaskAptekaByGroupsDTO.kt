package com.apteka.portal.report.dtos.response.aptekaproblems

import io.swagger.v3.oas.annotations.media.Schema
import java.util.UUID

@Schema(description = "Плоская строка агрегации задач аптек по группам, категориям и видам работ")
data class FlatTaskAptekaByGroupsDTO(
    @Schema(description = "Идентификатор группы аптек")
    val aptekaGroupId: Int,
    @Schema(description = "Наименование группы аптек")
    val aptekaGroupName: String,

    @Schema(description = "Идентификатор аптеки")
    val aptekaId: UUID,
    @Schema(description = "Наименование аптеки")
    val aptekaName: String,

    @Schema(description = "Идентификатор категории задач (group_task)")
    val groupTaskId: Int,
    @Schema(description = "Наименование категории задач")
    val groupTaskName: String,

    @Schema(description = "Идентификатор вида работ")
    val workTypeId: Int,
    @Schema(description = "Наименование вида работ")
    val workTypeName: String,

    @Schema(description = "Всего задач")
    val totalTasks: Long,
    @Schema(description = "Выполненные задачи (CLOSED)")
    val completedTasks: Long,
    @Schema(description = "Отклонённые задачи (DENIED)")
    val deniedTasks: Long,
    @Schema(description = "Процент выполненных задач")
    val percentCompletedTasks: Double,
    @Schema(description = "Процент отклонённых задач")
    val percentDeniedTasks: Double
)
