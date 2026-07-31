package com.apteka.portal.report.dtos.response.aptekaproblems

data class TaskAptekaWorkTypeReportDTO(
    val workTypeId: Int,
    val workTypeName: String,

    val totalTasks: Long,
    val completedTasks: Long,
    val deniedTasks: Long,
    val percentCompletedTasks: Double,
    val percentDeniedTasks: Double,
    val percentAll: Double
)
