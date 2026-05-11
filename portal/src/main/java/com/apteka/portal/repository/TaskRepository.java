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
public interface TaskRepository extends JpaRepository<Task, Long> {

    @Query("""
                SELECT DISTINCT t FROM Task t
                JOIN FETCH t.workType w
                JOIN FETCH w.groupTask gt
                JOIN gt.userGroup ug
                WHERE ug.id = :groupId
                    AND (:creatorClientId IS NULL OR t.createdByClient.id = :creatorClientId)
                    AND (:creatorAptekaId IS NULL OR t.createdByApteka.id = :creatorAptekaId)
                    AND (:specificClientId IS NULL OR t.assignedClient.id = :specificClientId)
                    AND (:specificAptekaId IS NULL OR t.assignedApteka.id = :specificAptekaId)
                    AND (:status IS NULL OR t.status = :status)
                    AND (:priority IS NULL OR t.priority = :priority)
                    AND (:groupTaskId IS NULL OR gt.id = :groupTaskId)
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
                    t.assignedClient.id,
                    COUNT(t),
                    SUM(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.OPEN THEN 1L ELSE 0L END),
                    SUM(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.CLOSED THEN 1L ELSE 0L END),
                    SUM(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.DENIED THEN 1L ELSE 0L END),
                    SUM(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.PROCESSED THEN 1L ELSE 0L END)
                )
                FROM Task t
                WHERE t.assignedClient.id IN :clientIds
                GROUP BY t.assignedClient.id
            """)
    List<TaskStatsDTO> getClientTaskStatsBatch(
            @Param("clientIds") List<UUID> clientId);

    @Query("""
                SELECT t.assignedClient.id,
                       COUNT(t),
                       SUM(CASE WHEN t.status IN :statuses THEN 1 ELSE 0 END)
                FROM Task t
                JOIN t.workType w
                JOIN w.groupTask gt
                WHERE gt.userGroup.id = :groupId
                GROUP BY t.assignedClient.id
            """)
    List<Object[]> getDepartmentPerformance(
            @Param("groupId") Integer groupId,
            @Param("statuses") List<TaskStatus> statuses);

    @Query("""
                SELECT new com.apteka.portal.dtos.response.GroupTaskStatsDTO(
                    ug.id,
                    ug.name,
                    SUM(CASE WHEN str(t.status) IN ('OPEN', 'PROCESSED') THEN 1L ELSE 0L END),
                    SUM(CASE WHEN str(t.status) = 'CLOSE' THEN 1L ELSE 0L END),
                    COUNT(t)
                )
                FROM Task t
                JOIN t.workType w
                JOIN w.groupTask gt
                JOIN gt.userGroup ug
                GROUP BY ug.id, ug.name
            """)
    List<GroupTaskStatsDTO> getGroupUserStats();
}
