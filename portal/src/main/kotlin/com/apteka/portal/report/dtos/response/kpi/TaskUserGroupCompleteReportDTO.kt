package com.apteka.portal.report.dtos.response.kpi

data class TaskUserGroupCompleteReportDTO(
    val totalTask: Long,
    val completedTask: Long,
    val deniedTask: Long,
    val closedPercentage: Double,
    val deniedPercentage: Double,
    val percentageAllTask: Double
)