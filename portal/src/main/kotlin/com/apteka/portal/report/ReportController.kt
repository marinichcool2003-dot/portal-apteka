package com.apteka.portal.report

import com.apteka.portal.models.AppUserDetails
import com.apteka.portal.report.dtos.request.ReportTaskByGroupRequestDTO
import com.apteka.portal.report.dtos.request.ReportTimeRequestDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaGroupReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserTaskReportDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Отчёты", description = "KPI по задачам сотрудников и отчёт по основным проблемам аптек")
@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService
) {
    @Operation(
        summary = "KPI-отчёт по задачам сотрудников",
        description = "Агрегирует закрытые и отклонённые задачи исполнителей (сотрудников) за период, сгруппированные по группам пользователей."
    )
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

    @Operation(
        summary = "KPI-отчёт по задачам сотрудников в группе",
        description = "То же, что KPI-отчёт, но только для указанной группы пользователей (groupId)."
    )
    @PostMapping("/task-kpi-report/by-group/{groupId}")
    fun getTaskKpiReportByGroup(
        @PathVariable groupId: Int,
        @RequestBody requestDTO: ReportTaskByGroupRequestDTO,
        @AuthenticationPrincipal currentUser: AppUserDetails
    ): ResponseEntity<Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>>> {
        // AUDIT-FIX: берём groupId из path, чтобы совпадал с URL
        val effectiveRequest = requestDTO.copy(groupId = groupId)
        val reportData = reportService.getGroupedTaskReportByGroupId(effectiveRequest)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }

    @Operation(
        summary = "Отчёт по основным проблемам аптек",
        description = "Строит иерархию Группа аптек → Аптека → Категория задач → Вид работ. Учитываются только задачи, созданные аккаунтом аптеки (creator → apteka)."
    )
    @PostMapping("/apteka-problems-report")
    fun getAptekaProblemsReport(
        @RequestBody requestDTO: ReportTimeRequestDTO
    ): ResponseEntity<List<TaskAptekaGroupReportDTO>> {
        val reportData = reportService.getAptekaProblemsReport(requestDTO)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }

    @Operation(
        summary = "Отчёт по основным проблемам аптек в группе",
        description = "Отчёт проблем аптек, ограниченный одной группой аптек (groupId)."
    )
    @PostMapping("/apteka-problems-report/by-group/{groupId}")
    fun getAptekaProblemsReportByGroup(
        @PathVariable groupId: Int,
        @RequestBody requestDTO: ReportTaskByGroupRequestDTO,
        @AuthenticationPrincipal currentUser: AppUserDetails
    ): ResponseEntity<List<TaskAptekaGroupReportDTO>> {
        // AUDIT-FIX: берём groupId из path, чтобы совпадал с URL
        val effectiveRequest = requestDTO.copy(groupId = groupId)
        val reportData = reportService.getAptekaProblemsReportByGroup(effectiveRequest)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }
}
