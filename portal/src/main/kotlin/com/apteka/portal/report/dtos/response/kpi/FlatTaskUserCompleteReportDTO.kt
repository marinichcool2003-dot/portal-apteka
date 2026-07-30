package com.apteka.portal.report.dtos.response.kpi

import java.util.UUID

data class FlatTaskUserCompleteReportDTO (
    val groupId: Int,
    val groupName: String,

    val clientId: UUID,
    val clientFullName: String,

    val totalTask: Long,
    val completedTask: Long,
    val deniedTask: Long,
    val percentageClosedDenied: Double,
    val percentageGroupTask: Double
)