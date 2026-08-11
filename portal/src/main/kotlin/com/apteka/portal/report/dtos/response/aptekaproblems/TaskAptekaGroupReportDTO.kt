package com.apteka.portal.report.dtos.response.aptekaproblems

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Отчёт по основным проблемам аптек на уровне группы")
data class TaskAptekaGroupReportDTO(
    @Schema(description = "Группа аптек")
    val userGroup: UserGroupShortResponseDTO,

    @Schema(description = "Всего задач в группе")
    val totalTasks: Long,
    @Schema(description = "Выполненные задачи")
    val completedTasks: Long,
    @Schema(description = "Отклонённые задачи")
    val deniedTasks: Long,
    @Schema(description = "Процент выполненных")
    val percentCompletedTasks: Double,
    @Schema(description = "Процент отклонённых")
    val percentDeniedTasks: Double,
    @Schema(description = "Доля задач группы от всех задач периода, %")
    val percentAll: Double,

    @Schema(description = "Аптеки группы")
    val aptekas: List<TaskAptekaReportDTO>
)
