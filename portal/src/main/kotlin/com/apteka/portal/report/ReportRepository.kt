import com.apteka.portal.models.Task
import com.apteka.portal.report.dtos.response.kpi.FlatTaskUserCompleteReportDTO
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ReportRepository : JpaRepository<Task, Long> {
    @Query(
        """
            SELECT new com.apteka.portal.report.dtos.response.kpi.FlatTaskUserCompleteReportDTO(
                ug.id,
                ug.name,
                acc.id,
                cl.fullName,
                COUNT(t),
                SUM(CASE WHEN t.status = 'CLOSED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'DENIED' THEN 1 ELSE 0 END), 
                CASE WHEN COUNT(t) > 0
                    THEN (SUM(CASE WHEN t.status = 'CLOSED' THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(t)
                    ELSE 0.0
                END,
                CASE WHEN COUNT(t) > 0
                    THEN (SUM(CASE WHEN t.status = 'DENIED' THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(t)
                    ELSE 0.0
                END
            ) 
            FROM Task t
            JOIN FETCH t.account acc
            JOIN FETCH acc.userGroup ug
            JOIN FETCH acc.client cl
            WHERE t.closedDate BETWEEN :startDate AND :endDate
            GROUP BY t.account.id, t.account.client.fullName
    """
    )
    fun countTaskUserCompleteReport(@Param("startDate") startDate: Instant,
                                    @Param("endDate") endDAte: Instant): List<FlatTaskUserCompleteReportDTO>

    @Query("SELECT COUNT(t) FROM Task t")
    fun countAllTask(): Long

    @Query("""
            SELECT new com.apteka.portal.report.dtos.response.kpi.FlatTaskUserCompleteReportDTO(
                ug.id,
                ug.name,
                acc.id,
                cl.fullName,
                COUNT(t),
                SUM(CASE WHEN t.status = 'CLOSED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'DENIED' THEN 1 ELSE 0 END), 
                CASE WHEN COUNT(t) > 0
                    THEN (SUM(CASE WHEN t.status = 'CLOSED' THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(t)
                    ELSE 0.0
                END,
                CASE WHEN COUNT(t) > 0
                    THEN (SUM(CASE WHEN t.status = 'DENIED' THEN 1.0 ELSE 0.0 END) * 100.0) / COUNT(t)
                    ELSE 0.0
                END
            ) 
            FROM Task t
            JOIN FETCH t.account acc
            JOIN FETCH acc.userGroup ug
            JOIN FETCH acc.client cl
            WHERE ug.id = :userGroupId
                AND t.closedDate BETWEEN :startDate AND :endDate
            GROUP BY t.account.id, t.account.client.fullName
    """)
    fun countTaskUserCompleteReportByGroup(@Param("startDate") startDate: Instant,
                                           @Param("endDate") endDAte: Instant,
                                           @Param("userGroupId") userGroupId: Int): List<FlatTaskUserCompleteReportDTO>

}