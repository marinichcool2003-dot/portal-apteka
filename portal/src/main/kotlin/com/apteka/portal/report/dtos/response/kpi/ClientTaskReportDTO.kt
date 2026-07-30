package com.apteka.portal.report.dtos.response.kpi

data class ClientTaskReportDTO(
    val totalTask: Long,
    val completedTask: Long,
    val deniedTask: Long,
    val percentageClosedDenied: Double,
    val percentageGroupTask: Double
)
