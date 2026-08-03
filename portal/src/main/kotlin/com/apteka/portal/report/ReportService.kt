package com.apteka.portal.report

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO
import com.apteka.portal.models.UserGroupType
import com.apteka.portal.report.dtos.request.ReportTaskByGroupRequestDTO
import com.apteka.portal.report.dtos.request.ReportTimeRequestDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.FlatTaskAptekaByGroupsDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaGroupReportDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaReportDTO
import com.apteka.portal.report.dtos.response.aptekaproblems.TaskAptekaWorkTypeReportDTO
import com.apteka.portal.report.dtos.response.kpi.ClientTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.FlatTaskUserCompleteReportDTO
import com.apteka.portal.report.dtos.response.kpi.TaskUserGroupCompleteReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserTaskReportDTO
import org.springframework.stereotype.Service

@Service
class ReportService(private var reportRepository: ReportRepository) {

    fun getAllGroupedTaskReports(reportTimeRequestDTO: ReportTimeRequestDTO): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>> {
        val flatRows = reportRepository.countTaskUserCompleteReport(reportTimeRequestDTO.startDate, reportTimeRequestDTO.endDate)
        return getGroupedTaskReports(flatRows)
    }

    fun getGroupedTaskReportByGroupId(reportTaskByGroupRequestDTO: ReportTaskByGroupRequestDTO): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>> {
        val flatRows = reportRepository.countTaskUserCompleteReportByGroup(
            reportTaskByGroupRequestDTO.startDate,
            reportTaskByGroupRequestDTO.endDate,
            reportTaskByGroupRequestDTO.groupId
        )
        return getGroupedTaskReports(flatRows)
    }

    // AUDIT-FIX: отчёт основных проблем аптек за период
    fun getAptekaProblemsReport(reportTimeRequestDTO: ReportTimeRequestDTO): List<TaskAptekaGroupReportDTO> {
        val flatRows = reportRepository.getAptekaProblemsReport(
            reportTimeRequestDTO.startDate,
            reportTimeRequestDTO.endDate
        )
        return groupAptekaProblems(flatRows)
    }

    // AUDIT-FIX: отчёт основных проблем аптек по группе
    fun getAptekaProblemsReportByGroup(reportTaskByGroupRequestDTO: ReportTaskByGroupRequestDTO): List<TaskAptekaGroupReportDTO> {
        val flatRows = reportRepository.getAptekaProblemsReportByGroup(
            reportTaskByGroupRequestDTO.startDate,
            reportTaskByGroupRequestDTO.endDate,
            reportTaskByGroupRequestDTO.groupId
        )
        return groupAptekaProblems(flatRows)
    }

    private fun getGroupedTaskReports(flatRows: List<FlatTaskUserCompleteReportDTO>): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>> {
        val countAllTask = reportRepository.countAllTask()

        val groupedByGroup = flatRows.groupBy { Pair(it.groupId, it.groupName) }

        return groupedByGroup.map { (groupPair, rowsInGroup) ->
            val (groupId, groupName) = groupPair

            val userReports = rowsInGroup.map { row ->
                UserTaskReportDTO(
                    clientId = row.clientId,
                    fullName = row.clientFullName,
                    clientTaskReportDTO = ClientTaskReportDTO(
                        totalTask = row.totalTask,
                        completedTask = row.completedTask,
                        deniedTask = row.deniedTask,
                        percentageClosedDenied = row.percentageClosedDenied,
                        percentageGroupTask = row.percentageGroupTask
                    )
                )
            }
            val totalGroupTasks = userReports.sumOf { it.clientTaskReportDTO.totalTask }
            val closedGroupTasks = userReports.sumOf { it.clientTaskReportDTO.completedTask }
            val deniedGroupTasks = userReports.sumOf { it.clientTaskReportDTO.deniedTask }

            val closedGroupPct = if (totalGroupTasks > 0) (closedGroupTasks.toDouble() * 100.0) / totalGroupTasks else 0.0
            val deniedGroupPct = if (totalGroupTasks > 0) (deniedGroupTasks.toDouble() * 100.0) / totalGroupTasks else 0.0
            val countAllTaskPct = if (countAllTask > 0) (totalGroupTasks.toDouble() * 100.0) / countAllTask else 0.0

            val groupKey = UserGroupTaskReportDTO(
                // AUDIT-FIX: UserGroupShortResponseDTO теперь включает groupType; для KPI сотрудников — EMPLOYEE_GROUP
                userGroup = UserGroupShortResponseDTO(groupId, groupName, UserGroupType.EMPLOYEE_GROUP),
                taskUserGroupCompleteReport = TaskUserGroupCompleteReportDTO(
                    totalTask = totalGroupTasks,
                    completedTask = closedGroupTasks,
                    deniedTask = deniedGroupTasks,
                    closedPercentage = closedGroupPct,
                    deniedPercentage = deniedGroupPct,
                    percentageAllTask = countAllTaskPct
                )
            )
            groupKey to userReports
        }.toMap()
    }

    // AUDIT-FIX: группировка flat → Group → Apteka → GroupTask → WorkType
    private fun groupAptekaProblems(flatRows: List<FlatTaskAptekaByGroupsDTO>): List<TaskAptekaGroupReportDTO> {
        if (flatRows.isEmpty()) {
            return emptyList()
        }

        val countAllTask = reportRepository.countAllTask()

        return flatRows
            .groupBy { Pair(it.aptekaGroupId, it.aptekaGroupName) }
            .map { (groupPair, groupRows) ->
                val (groupId, groupName) = groupPair

                val aptekas = groupRows
                    .groupBy { Pair(it.aptekaId, it.aptekaName) }
                    .map { (aptekaPair, aptekaRows) ->
                        val (aptekaId, aptekaName) = aptekaPair

                        val groupTasks = aptekaRows
                            .groupBy { Pair(it.groupTaskId, it.groupTaskName) }
                            .map { (gtPair, gtRows) ->
                                val (groupTaskId, groupTaskName) = gtPair

                                val workTypes = gtRows.map { row ->
                                    val wtTotal = row.totalTasks
                                    TaskAptekaWorkTypeReportDTO(
                                        workTypeId = row.workTypeId,
                                        workTypeName = row.workTypeName,
                                        totalTasks = wtTotal,
                                        completedTasks = row.completedTasks,
                                        deniedTasks = row.deniedTasks,
                                        percentCompletedTasks = row.percentCompletedTasks,
                                        percentDeniedTasks = row.percentDeniedTasks,
                                        percentAll = if (countAllTask > 0) (wtTotal.toDouble() * 100.0) / countAllTask else 0.0
                                    )
                                }

                                val gtTotal = workTypes.sumOf { it.totalTasks }
                                val gtCompleted = workTypes.sumOf { it.completedTasks }
                                val gtDenied = workTypes.sumOf { it.deniedTasks }

                                TaskAptekaGroupTaskReportDTO(
                                    groupTaskId = groupTaskId,
                                    groupTaskName = groupTaskName,
                                    totalTasks = gtTotal,
                                    completedTasks = gtCompleted,
                                    deniedTasks = gtDenied,
                                    percentCompletedTasks = pct(gtCompleted, gtTotal),
                                    percentDeniedTasks = pct(gtDenied, gtTotal),
                                    percentAll = if (countAllTask > 0) (gtTotal.toDouble() * 100.0) / countAllTask else 0.0,
                                    workTypes = workTypes
                                )
                            }

                        val aptekaTotal = groupTasks.sumOf { it.totalTasks }
                        val aptekaCompleted = groupTasks.sumOf { it.completedTasks }
                        val aptekaDenied = groupTasks.sumOf { it.deniedTasks }

                        TaskAptekaReportDTO(
                            aptekaId = aptekaId,
                            aptekaName = aptekaName,
                            totalTasks = aptekaTotal,
                            completedTasks = aptekaCompleted,
                            deniedTasks = aptekaDenied,
                            percentCompletedTasks = pct(aptekaCompleted, aptekaTotal),
                            percentDeniedTasks = pct(aptekaDenied, aptekaTotal),
                            percentAll = if (countAllTask > 0) (aptekaTotal.toDouble() * 100.0) / countAllTask else 0.0,
                            groupTasks = groupTasks
                        )
                    }

                val groupTotal = aptekas.sumOf { it.totalTasks }
                val groupCompleted = aptekas.sumOf { it.completedTasks }
                val groupDenied = aptekas.sumOf { it.deniedTasks }

                TaskAptekaGroupReportDTO(
                    userGroup = UserGroupShortResponseDTO(groupId, groupName, UserGroupType.APTEKA_GROUP),
                    totalTasks = groupTotal,
                    completedTasks = groupCompleted,
                    deniedTasks = groupDenied,
                    percentCompletedTasks = pct(groupCompleted, groupTotal),
                    percentDeniedTasks = pct(groupDenied, groupTotal),
                    percentAll = if (countAllTask > 0) (groupTotal.toDouble() * 100.0) / countAllTask else 0.0,
                    aptekas = aptekas
                )
            }
    }

    private fun pct(part: Long, total: Long): Double =
        if (total > 0) (part.toDouble() * 100.0) / total else 0.0
}
