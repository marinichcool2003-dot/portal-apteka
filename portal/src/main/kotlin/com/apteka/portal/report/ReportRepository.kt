package com.apteka.portal.report

import com.apteka.portal.models.Task
import com.apteka.portal.report.dtos.response.aptekaproblems.FlatTaskAptekaCreateReportDTO
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
            JOIN t.assigner acc
            JOIN acc.userGroup ug
            JOIN acc.client cl
            WHERE t.closingDate BETWEEN :startDate AND :endDate
            GROUP BY acc.id, cl.fullName
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
            JOIN t.assigner acc
            JOIN acc.userGroup ug
            JOIN acc.client cl
            WHERE ug.id = :userGroupId
                AND t.closingDate BETWEEN :startDate AND :endDate
            GROUP BY acc.id, cl.fullName
    """)
    fun countTaskUserCompleteReportByGroup(@Param("startDate") startDate: Instant,
                                           @Param("endDate") endDAte: Instant,
                                           @Param("userGroupId") userGroupId: Int): List<FlatTaskUserCompleteReportDTO>

    @Query("""
        SELECT new com.apteka.portal.report.dtos.response.aptekaproblems.FlatTaskAptekaByGroupsDTO(
            ug.id,
            ug.name,
            a.id,
            a.aptekaName,
            gt.id,
            gt.name,
            wt.id,
            wt.name,
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
        JOIN t.assigner ass
        JOIN ass.apteka a
        JOIN ass.userGroup ug
        JOIN t.workType wt
        JOIN wt.groupTask gt
        JOIN gt.creatorGroup cg
        WHERE t.closingDate BETWEEN :startDate AND :endDate
        GROUP BY a.id, a.aptekaName
    """)
    fun getTaskAptekaCreateReport(@Param("startDate") startDate: Instant,
                                  @Param("endDate") endDate: Instant) : List<FlatTaskAptekaCreateReportDTO>

}