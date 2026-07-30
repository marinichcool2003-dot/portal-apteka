package com.apteka.portal.report

import ReportService
import com.apteka.portal.models.AppUserDetails
import com.apteka.portal.report.dtos.request.ReportTaskByGroupRequestDTO
import com.apteka.portal.report.dtos.request.ReportTimeRequestDTO
import com.apteka.portal.report.dtos.response.kpi.UserGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserTaskReportDTO
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService
) {
    @PostMapping("/task-kpi-report")
    fun getTaskKpiReport(
        @RequestBody requestDTO: ReportTimeRequestDTO
    ): ResponseEntity<Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>>> {
        val reportData = reportService.getAllGroupedTaskReports(requestDTO)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }

    @PostMapping("/task-kpi-report/by-group/{groupId}")
    fun getTaskKpiReportByGroup(
        @PathVariable groupId: Int,
        @RequestBody requestDTO: ReportTaskByGroupRequestDTO,
        @AuthenticationPrincipal currentUser: AppUserDetails
    ): ResponseEntity<Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>>> {
        val reportData = reportService.getGroupedTaskReportByGroupId(requestDTO)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }
}