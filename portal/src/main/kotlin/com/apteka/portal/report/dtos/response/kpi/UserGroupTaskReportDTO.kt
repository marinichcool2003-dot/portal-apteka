package com.apteka.portal.report.dtos.response.kpi

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "KPI-отчёт по задачам группы пользователей")
data class UserGroupTaskReportDTO(
    @Schema(description = "Группа пользователей")
    val userGroup: UserGroupShortResponseDTO,
    @Schema(description = "Отчёт по выполнению задач группы")
    val taskUserGroupCompleteReport: TaskUserGroupCompleteReportDTO
    )
