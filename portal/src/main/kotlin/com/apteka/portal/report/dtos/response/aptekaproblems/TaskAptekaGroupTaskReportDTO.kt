package com.apteka.portal.report.dtos.response.aptekaproblems

data class TaskAptekaGroupTaskReportDTO(
    val groupTaskId: Int,
    val groupTaskName: String,

    val totalTasks: Long,
    val completedTasks: Long,
    val deniedTasks: Long,
    val percentCompletedTasks: Double,
    val percentDeniedTasks: Double,
    val percentAll: Double,

    val taskAptekaWorkTypeReportDTO: List<TaskAptekaWorkTypeReportDTO>
)
