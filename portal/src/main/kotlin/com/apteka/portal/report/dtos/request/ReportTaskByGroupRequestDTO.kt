package com.apteka.portal.report.dtos.request

import java.time.Instant

data class ReportTaskByGroupRequestDTO(
    val groupId: Int,
    override val startDate: Instant,
    override val endDate: Instant
) : ReportTimeRequestDTO(startDate, endDate)