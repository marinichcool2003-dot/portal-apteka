import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO
import com.apteka.portal.report.dtos.request.ReportTaskByGroupRequestDTO
import com.apteka.portal.report.dtos.response.kpi.ClientTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.TaskUserGroupCompleteReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserGroupTaskReportDTO
import com.apteka.portal.report.dtos.response.kpi.UserTaskReportDTO
import com.apteka.portal.report.dtos.request.ReportTimeRequestDTO
import com.apteka.portal.report.dtos.response.kpi.FlatTaskUserCompleteReportDTO
import org.springframework.stereotype.Service

@Service
class ReportService(private var reportRepository: ReportRepository) {

    fun getAllGroupedTaskReports(reportTimeRequestDTO: ReportTimeRequestDTO): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>> {
        val flatRows = reportRepository.countTaskUserCompleteReport(reportTimeRequestDTO.startDate, reportTimeRequestDTO.endDate)
        return getGroupedTaskReports(flatRows)
    }

    fun getGroupedTaskReportByGroupId(reportTaskByGroupRequestDTO: ReportTaskByGroupRequestDTO): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>> {
        val flatRows = reportRepository.countTaskUserCompleteReportByGroup(reportTaskByGroupRequestDTO.startDate,
            reportTaskByGroupRequestDTO.endDate, reportTaskByGroupRequestDTO.groupId)
        return getGroupedTaskReports(flatRows)
    }

    private fun getGroupedTaskReports(flatRows: List<FlatTaskUserCompleteReportDTO>): Map<UserGroupTaskReportDTO, List<UserTaskReportDTO>>  {
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
                userGroup = UserGroupShortResponseDTO(groupId, groupName),
                userGroupTaskReport = TaskUserGroupCompleteReportDTO(
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
}