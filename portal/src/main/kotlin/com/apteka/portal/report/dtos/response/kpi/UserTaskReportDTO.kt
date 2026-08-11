package com.apteka.portal.report.dtos.response.kpi

import java.util.UUID
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "KPI-отчёт по задачам пользователя")
data  class UserTaskReportDTO (
    @Schema(description = "Идентификатор клиента")
    val clientId: UUID,
    @Schema(description = "ФИО пользователя")
    val fullName: String,
    @Schema(description = "Отчёт по задачам клиента")
    val clientTaskReportDTO: ClientTaskReportDTO
)