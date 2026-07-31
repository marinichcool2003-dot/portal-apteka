package com.apteka.portal.report.dtos.response.kpi

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO

data class UserGroupTaskReportDTO(
    val userGroup: UserGroupShortResponseDTO,
    val taskUserGroupCompleteReport: TaskUserGroupCompleteReportDTO
    )
