package com.apteka.portal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apteka.portal.dtos.response.GroupTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

@Repository
public interface TaskInterface extends JpaRepository<Task, Long> {

    @Query("""
                SELECT DISTINCT t FROM Task t
                JOIN FETCH t.workType w
                JOIN FETCH w.groupTask g
                WHERE t.assignedGroup.id = :groupId
                    AND(:creatorClientId IS NULL OR t.createdByClient.id = :creatorClientId)
                    AND(:creatorAptekaId IS NULL OR t.createdByApteka.id = :creatorAptekaId)
                    AND (:specificClientId IS NULL OR t.assignedClient.id = :specificClientId)
                    AND (:specificAptekaId IS NULL OR t.assignedApteka.id = :specificAptekaId)
                    AND (:status IS NULL OR t.status = :status)
                    AND (:priority IS NULL OR t.priority = :priority)
                    AND (:groupTaskId IS NULL OR g.id = :groupTaskId)
                ORDER BY t.creationDate DESC
            """)
    List<Task> findDepartmentTasksWithFilters(
            @Param("groupId") Integer groupId,
            @Param("creatorClientId") UUID creatorClientId,
            @Param("creatorAptekaId") Integer creatorAptekaId,
            @Param("specificClientId") UUID specificClientId,
            @Param("specificAptekaId") Integer specificAptekaId,
            @Param("status") TaskStatus status,
            @Param("priority") TaskPriority priority,
            @Param("groupTaskId") Integer groupTaskId);

    @Query("""
            SELECT new com.apteka.portal.dtos.response.TaskStatsDTO(
                COUNT(t),
                SUM(CASE WHEN t.status = 'OPEN' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'CLOSE' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'DENIED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN t.status = 'PROCESSED' THEN 1 ELSE 0 END)
            )
            FROM Task t
            WHERE t.assignedClient.id = :clientId
            """)
    TaskStatsDTO getClientTaskStats(@Param("clientId") UUID clientId);

    @Query("""
                SELECT t.assignedClient.id,
                       COUNT(t),
                       SUM(CASE WHEN t.status IN ('OPEN', 'PROCESSED') THEN 1 ELSE 0 END)
                FROM Task t
                WHERE t.assignedGroup.id = :groupId
                GROUP BY t.assignedClient.id
            """)
    List<Object[]> getDepartmentPerformance(@Param("groupId") Integer groupId);

    @Query("""
                SELECT new com.apteka.portal.dtos.response.GroupTaskStatsDTO(
                    g.id,
                    g.name,
                    SUM(CASE WHEN t.status IN ('OPEN', 'PROCESSED') THEN 1 ELSE 0 END),
                    SUM(CASE WHEN t.status = 'CLOSE' THEN 1 ELSE 0 END),
                    COUNT(t)
                )
                FROM Task t
                JOIN t.assignedGroup g
                GROUP BY g.id, g.name
            """)
    List<GroupTaskStatsDTO> getGroupUserStats();
}
