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

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			LEFT JOIN FETCH t.createdByClient
			LEFT JOIN FETCH t.createdByApteka cba
			LEFT JOIN FETCH t.assignedClient
			LEFT JOIN FETCH t.assignedApteka aa
			WHERE t.id IN :ids
			""")
	List<Task> findShortTasksByIds(@Param("ids") List<Long> ids);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.TaskStatsDTO(
			        t.assignedClient.id,
			        COUNT(t),
			        COUNT(CASE WHEN CAST(t.status as string) = 'OPEN' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'CLOSED' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'DENIED' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'PROCESSED' THEN 1 END)
			    )
			    FROM Task t
			    WHERE t.assignedClient.id IN :clientIds
			    GROUP BY t.assignedClient.id
			""")
	List<TaskStatsDTO> getClientTaskStatsBatch(@Param("clientIds") List<UUID> clientIds);

	@Query("""
			    SELECT t.assignedClient.id,
			           COUNT(t),
			           COUNT(CASE WHEN t.status IN :statuses THEN 1 END)
			    FROM Task t
			    JOIN t.workType w
			    JOIN w.groupTask gt
			    WHERE gt.userGroup.id = :groupId
			    GROUP BY t.assignedClient.id
			""")
	List<Object[]> getDepartmentPerformance(
			@Param("groupId") Integer groupId,
			@Param("statuses") List<String> statuses);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.GroupTaskStatsDTO(
			        ug.id,
			        ug.name,
			        COUNT(CASE WHEN CAST(t.status as string) IN ('OPEN', 'PROCESSED') THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'CLOSED' THEN 1 END),
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
