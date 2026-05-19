package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apteka.portal.dtos.response.GroupTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskStatsDTO;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@Query("""
			SELECT t.id FROM Task t
			JOIN t.workType w
			JOIN w.groupTask gt
			LEFT JOIN gt.userGroup ug
			WHERE ug.id = :groupId
			    AND (:creatorClientId IS NULL OR t.createdByClient.id = :creatorClientId)
			    AND (:creatorAptekaId IS NULL OR t.createdByApteka.id = :creatorAptekaId)
			    AND (:specificClientId IS NULL OR t.assignedClient.id = :specificClientId)
			    AND (:specificAptekaId IS NULL OR t.assignedApteka.id = :specificAptekaId)
			    AND (CAST(:status as string) IS NULL OR t.status = :status)
			    AND (CAST(:priority as string) IS NULL OR t.priority = :priority)
			    AND (:groupTaskId IS NULL OR gt.id = :groupTaskId)
			ORDER BY t.creationDate DESC
			""")
	List<Long> findDepartmentTaskIdsWithFilters(
			@Param("groupId") Integer groupId,
			@Param("creatorClientId") UUID creatorClientId,
			@Param("creatorAptekaId") Integer creatorAptekaId,
			@Param("specificClientId") UUID specificClientId,
			@Param("specificAptekaId") Integer specificAptekaId,
			@Param("status") TaskStatus status,
			@Param("priority") TaskPriority priority,
			@Param("groupTaskId") Integer groupTaskId);

	@Query("""
			SELECT t.id FROM Task t
			JOIN t.workType w
			JOIN w.groupTask gt
			LEFT JOIN gt.userGroup ug
			WHERE ug.id = :groupId
				AND (:specificClientId IS NULL OR t.assignedClient.id = :specificClientId)
				AND (CAST(:status as string) IS NULL OR t.status = :status)
				AND (CAST(:priority as string) IS NULL OR t.priority = :priority)
				AND (:groupTaskid IS NULL OR gt.id = :groupTaskId)
			ORDER BY t.creationDate DESC
			""")
	List<Long> findTaskAssignedMe(
			@Param("specificClientId") UUID specificClientId,
			@Param("groupId") Integer groupId,
			@Param("status") TaskStatus status,
			@Param("priority") TaskPriority priority,
			@Param("groupTaskId") Integer groupTaskId);

	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			LEFT JOIN FETCH gt.userGroup ug
			LEFT JOIN FETCH t.createdByClient
			LEFT JOIN FETCH t.createdByApteka cba
			LEFT JOIN FETCH cba.userGroup
			LEFT JOIN FETCH t.assignedClient
			LEFT JOIN FETCH t.assignedApteka aa
			LEFT JOIN FETCH aa.userGroup
			WHERE t.id IN :ids
			""")
	List<Task> findTasksWithDetailsByIds(@Param("ids") List<Long> ids);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.TaskStatsDTO(
			        t.assignedClient.id,
			        COUNT(t),
			        COALESCE(SUM(CASE WHEN str(t.status) = 'OPEN' THEN 1L ELSE 0L END), 0L),
			        COALESCE(SUM(CASE WHEN str(t.status) = 'CLOSED' THEN 1L ELSE 0L END), 0L),
			        COALESCE(SUM(CASE WHEN str(t.status) = 'DENIED' THEN 1L ELSE 0L END), 0L),
			        COALESCE(SUM(CASE WHEN str(t.status) = 'PROCESSED' THEN 1L ELSE 0L END), 0L)
			    )
			    FROM Task t
			    WHERE t.assignedClient.id IN :clientIds
			    GROUP BY t.assignedClient.id
			""")
	List<TaskStatsDTO> getClientTaskStatsBatch(@Param("clientIds") List<UUID> clientIds);

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
			        SUM(CASE WHEN str(t.status) = 'CLOSED' THEN 1L ELSE 0L END),
			        COUNT(t)
			    )
			    FROM Task t
			    JOIN t.workType w
			    JOIN w.groupTask gt
			    JOIN gt.userGroup ug
			    GROUP BY ug.id, ug.name
			""")
	List<GroupTaskStatsDTO> getGroupUserStats();

	@Query("""
			    SELECT DISTINCT t FROM Task t
			    JOIN FETCH t.workType w
			    JOIN FETCH w.groupTask gt
			    LEFT JOIN FETCH gt.userGroup ug
			    LEFT JOIN FETCH t.createdByClient
			    LEFT JOIN FETCH t.createdByApteka cba
			    LEFT JOIN FETCH cba.userGroup
			    LEFT JOIN FETCH t.assignedClient
			    LEFT JOIN FETCH t.assignedApteka aa
			    LEFT JOIN FETCH aa.userGroup
			    LEFT JOIN FETCH t.pictures
			    WHERE t.id = :id
			""")
	Optional<Task> findByIdWithDetailsAndPictures(@Param("id") Long id);

	@Query("""
			    SELECT t FROM Task t
			    LEFT JOIN FETCH t.employeeComments ec
			    LEFT JOIN FETCH ec.client
			    LEFT JOIN FETCH ec.apteka a
			    LEFT JOIN FETCH a.userGroup
			    WHERE t.id = :id
			""")
	Optional<Task> fetchCommentsForTask(@Param("id") Long id);

}
