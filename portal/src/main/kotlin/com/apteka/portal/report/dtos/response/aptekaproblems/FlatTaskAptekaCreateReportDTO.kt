package com.apteka.portal.report.dtos.response.aptekaproblems

import java.util.UUID

data class FlatTaskAptekaCreateReportDTO(
    val userGroupId: Int,
    val userGroupName: String,

    val aptekaId: UUID,
    val aptekaName: String,

)
