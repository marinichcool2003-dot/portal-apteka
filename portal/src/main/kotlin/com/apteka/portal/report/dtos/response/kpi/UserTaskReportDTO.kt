package com.apteka.portal.report.dtos.response.kpi

import java.util.UUID

data  class UserTaskReportDTO (
    val clientId: UUID,
    val fullName: String,
    val clientTaskReportDTO: ClientTaskReportDTO
)