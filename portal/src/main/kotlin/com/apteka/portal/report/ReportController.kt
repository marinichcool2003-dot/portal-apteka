package com.apteka.portal.report

import com.apteka.portal.models.AppUserDetails
import com.apteka.portal.report.dtos.request.ReportTaskByGroupRequestDTO
import com.apteka.portal.report.dtos.request.ReportTimeRequestDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaGroupReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserTaskReportDTO
import com.apteka.portal.services.DailyDepartmentReportService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@Tag(name = "Отчёты", description = "KPI по задачам сотрудников и отчёт по основным проблемам аптек")
@RestController
@RequestMapping("/api/v1/reports")
class ReportController(
    private val reportService: ReportService,
    private val dailyDepartmentReportService: DailyDepartmentReportService
) {
    @Operation(
        summary = "KPI-отчёт по задачам сотрудников",
        description = "Агрегирует назначенные задачи исполнителей за период по creationDate (UTC Instant). Считает total / CLOSED / DENIED и проценты; группировка по группам сотрудников. Пусто → 204."
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
        description = "То же, что KPI-отчёт (период по creationDate), но только для указанной группы сотрудников (path groupId). Пусто → 204."
    )
    @PostMapping("/task-kpi-report/by-group/{groupId}")
    fun getTaskKpiReportByGroup(
        @PathVariable groupId: Int,
        @RequestBody requestDTO: ReportTaskByGroupRequestDTO,
        @AuthenticationPrincipal currentUser: AppUserDetails
    ): ResponseEntity<Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>>> {
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
        description = "Иерархия Группа аптек → Аптека → Категория → Вид работ. Только задачи creator→apteka. Период фильтруется по creationDate (UTC Instant). Пусто → 204."
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
        description = "Отчёт проблем аптек за период по creationDate, ограниченный одной группой аптек (path groupId). Пусто → 204."
    )
    @PostMapping("/apteka-problems-report/by-group/{groupId}")
    fun getAptekaProblemsReportByGroup(
        @PathVariable groupId: Int,
        @RequestBody requestDTO: ReportTaskByGroupRequestDTO,
        @AuthenticationPrincipal currentUser: AppUserDetails
    ): ResponseEntity<List<TaskAptekaGroupReportDTO>> {
        val effectiveRequest = requestDTO.copy(groupId = groupId)
        val reportData = reportService.getAptekaProblemsReportByGroup(effectiveRequest)
        return if (reportData.isEmpty()) {
            ResponseEntity.noContent().build()
        } else {
            ResponseEntity.ok(reportData)
        }
    }

    @Operation(
        summary = "Запуск ежедневного отчёта по отделам",
        description = "ADMIN: формирует и отправляет ежедневный отчёт по отделам. Без date — за вчера в зоне app.report.daily.zone."
    )
    @PostMapping("/daily-department/run")
    @PreAuthorize("hasRole('ADMIN')")
    fun runDailyDepartmentReport(
        @RequestParam(required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        date: LocalDate?
    ): ResponseEntity<Map<String, Any>> {
        val result = if (date != null) {
            dailyDepartmentReportService.runForDate(date)
        } else {
            dailyDepartmentReportService.runForYesterday()
        }
        return ResponseEntity.ok(result)
    }
}
