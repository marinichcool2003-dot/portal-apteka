package com.apteka.portal.report.dtos.request

import java.time.Instant

open class ReportTimeRequestDTO(
    open val startDate: Instant,
    open val endDate: Instant
)
