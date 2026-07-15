package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.AssignedStatsDTO;
import com.apteka.portal.dtos.response.CreatedStatsDTO;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

	@EntityGraph(attributePaths = {
			"workType",
			"workType.groupTask",
			"creator",
			"assigner"
	})
	@Override
	Page<Task> findAll(Specification<Task> spec, Pageable pageable);

	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			LEFT JOIN FETCH t.creator
			LEFT JOIN FETCH t.assigner
			WHERE t.id IN :ids
			""")
	List<Task> findShortTasksByIds(@Param("ids") List<Long> ids);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.AssignedStatsDTO(
			        t.assigner.id,
			        COUNT(t),
			        COUNT(CASE WHEN CAST(t.status as string) = 'OPEN' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'CLOSED' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'DENIED' THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'PROCESSED' THEN 1 END)
			    )
			    FROM Task t
			    WHERE t.assigner.id IN :clientIds
			    GROUP BY t.assigner.id
			""")
	List<AssignedStatsDTO> getClientAssignedStatsBatch(@Param("clientIds") List<UUID> clientIds);

	@Query("""
				SELECT new com.apteka.portal.dtos.response.CreatedStatsDTO(
					t.creator.id,
				COUNT(t)
				)
				FROM Task t
				WHERE t.creator.id IN :clientIds AND t.status = 'OPEN'
				GROUP BY t.creator.id
			""")
	List<CreatedStatsDTO> getClientCreatedStatsBatch(@Param("clientIds") List<UUID> clientIds);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.DepartmentTaskStatsDTO(
			        ug.id,
			        ug.name,
			        COUNT(CASE WHEN CAST(t.status as string) IN ('OPEN', 'PROCESSED') THEN 1 END),
			        COUNT(CASE WHEN CAST(t.status as string) = 'CLOSED' THEN 1 END),
					COUNT(CASE WHEN CAST(t.status as string) = 'DENIED' THEN 1 END),
			        COUNT(t)
			    )
			    FROM Task t
			    JOIN t.workType w
			    JOIN w.groupTask gt
			    JOIN gt.userGroup ug
			    GROUP BY ug.id, ug.name
			""")
	List<DepartmentTaskStatsDTO> findGroupUserStats();

	@Query("""
			SELECT new com.apteka.portal.dtos.response.DepartmentTaskStatsDTO(
				ug.id,
				ug.name,
				COUNT(CASE WHEN CAST(t.status as string) IN ('OPEN', 'PROCESSED') THEN 1 END),
			    COUNT(CASE WHEN CAST(t.status as string) = 'CLOSED' THEN 1 END),
				COUNT(CASE WHEN CAST(t.status as string) = 'DENIED' THEN 1 END),
			    COUNT(t)
			)
			FROM Task t
			JOIN t.workType w
			JOIN w.groupTask gt
			JOIN gt.userGroup ug
			WHERE ug.id = :userGroupId
			GROUP BY ug.id, ug.name
			""")
	DepartmentTaskStatsDTO findGroupUserStatsByGroup(@Param("userGroupId") Integer userGroupId);

	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			LEFT JOIN FETCH gt.userGroup ug
			LEFT JOIN FETCH t.creator cre
			LEFT JOIN FETCH t.assigner ass
			INNER JOIN FETCH cre.client
			INNER JOIN FETCH cre.apteka
			INNER JOIN FETCH ass.client
			INNER JOIN FETCH ass.apteka
			LEFT JOIN FETCH cre.userGroup
			LEFT JOIN FETCH ass.userGroup
			WHERE t.id = :id
			""")
	Optional<Task> findByIdWithDetails(@Param("id") Long id);

	@Query("""
			SELECT t FROM Task t JOIN FETCH t.pictures WHERE t.id = :id
			""")
	Optional<Task> fetchPictures(@Param("id") Long id);

	@Query("""
			    SELECT t FROM Task t
			    LEFT JOIN FETCH t.employeeComments ec
			    LEFT JOIN FETCH ec.account acc
			    INNER JOIN FETCH acc.client
 				INNER JOIN FETCH acc.apteka
			    LEFT JOIN FETCH acc.userGroup
			    WHERE t.id = :id
			""")
	Optional<Task> fetchCommentsForTask(@Param("id") Long id);

	@Query("""
			SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
			FROM Task t
			WHERE (t.assignedClientId = :clientId AND t.status IN :statusCollection)
			   OR (t.createdByClientId = :clientId AND t.status IN :statusCollection)
			""")
	boolean existsByClientIdAndStatus(UUID clientId, Set<TaskStatus> statusCollection);

	@Query("""
			SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
			FROM Task t
			WHERE (t.assignedAptekaId = :aptekaId AND t.status IN :statusCollection)
				OR (t.createdByAptekaId = :aptekaId AND t.status IN :statusCollection)
			""")
	boolean existsByAptekaIdAndStatus(UUID aptekaId, Set<TaskStatus> statusCollection);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			JOIN wt.groupTask gt
			WHERE gt.id = :groupTaskId
			AND t.status IN ('OPEN', 'PROCESSED')
			""")
	boolean existsByGroupTaskAndStatusActive(Integer groupTaskId);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			JOIN wt.groupTask gt
			WHERE gt.id = :groupTaskId
			AND t.status IN ('DENIED', 'CLOSED')
			""")
	boolean existsByGroupTaskAndStatusNonActive(Integer groupTaskId);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			WHERE wt.id = :workTypeId
			AND t.status IN ('OPEN', 'PROCESSED')
			""")
	boolean existsByWorkTypeAndStatusActive(Integer workTypeId);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			WHERE wt.id = :workTypeId
			AND t.status IN ('DENIED', 'CLOSED')
			""")
	boolean existsByWorkTypeAndStatusNonActive(Integer workTypeId);
}
