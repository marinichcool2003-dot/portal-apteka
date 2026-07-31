package com.apteka.portal.report.dtos.response.aptekaproblems

import java.util.UUID

data class FlatTaskAptekaByGroupsDTO(
    val aptekaGroupId: Int,
    val aptekaGroupName: String,

    val aptekaId: UUID,
    val aptekaName: String,

    val groupTaskId: Int,
    val groupTaskName: String,

    val workTypeId: Int,
    val workTypeName: String,

    val totalTasks: Long,
    val completedTasks: Long,
    val deniedTasks: Long,
    val percentCompletedTasks: Double,
    val percentDeniedTasks: Double
)
