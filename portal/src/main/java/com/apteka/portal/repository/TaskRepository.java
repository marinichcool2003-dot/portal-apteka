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
			"creator.client",
			"creator.apteka",
			"assigner",
			"assigner.client",
			"assigner.apteka"
	})
	@Override
	Page<Task> findAll(Specification<Task> spec, Pageable pageable);

	@EntityGraph(attributePaths = {
			"workType",
			"workType.groupTask",
			"creator",
			"creator.client",
			"creator.apteka",
			"assigner",
			"assigner.client",
			"assigner.apteka"
	})
	@Override
	Page<Task> findAll(Pageable pageable);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.AssignedStatsDTO(
			        t.assigner.id,
			        COUNT(t),
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.OPEN THEN 1 END),
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.CLOSED THEN 1 END),
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.DENIED THEN 1 END),
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.PROCESSED THEN 1 END)
			    )
			    FROM Task t
			    WHERE t.assigner.id IN :clientIds
			    GROUP BY t.assigner.id
			""")
	List<AssignedStatsDTO> getAssignerStatsBatch(@Param("clientIds") List<UUID> clientIds);

	@Query("""
				SELECT new com.apteka.portal.dtos.response.CreatedStatsDTO(
					t.creator.id,
				COUNT(t)
				)
				FROM Task t
				WHERE t.creator.id IN :clientIds AND t.status = com.apteka.portal.models.TaskStatus.OPEN
				GROUP BY t.creator.id
			""")
	List<CreatedStatsDTO> getCreatorStatsBatch(@Param("clientIds") List<UUID> clientIds);

	@Query("""
			    SELECT new com.apteka.portal.dtos.response.DepartmentTaskStatsDTO(
			        ug.id,
			        ug.name,
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.OPEN OR t.status = com.apteka.portal.models.TaskStatus.PROCESSED THEN 1 END),
			        COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.CLOSED THEN 1 END),
					COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.DENIED THEN 1 END),
			        COUNT(t)
			    )
			    FROM Task t
			    JOIN t.workType w
			    JOIN w.groupTask gt
			    JOIN gt.intendedGroup ug
			    GROUP BY ug.id, ug.name
			""")
	// AUDIT-FIX: статистика по intended/executor группе (отдел-исполнитель), не creatorGroup
	List<DepartmentTaskStatsDTO> findGroupUserStats();

	@Query("""
			SELECT new com.apteka.portal.dtos.response.DepartmentTaskStatsDTO(
				ug.id,
				ug.name,
				COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.OPEN OR t.status = com.apteka.portal.models.TaskStatus.PROCESSED THEN 1 END),
			    COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.CLOSED THEN 1 END),
				COUNT(CASE WHEN t.status = com.apteka.portal.models.TaskStatus.DENIED THEN 1 END),
			    COUNT(t)
			)
			FROM Task t
			LEFT JOIN t.workType w
			LEFT JOIN w.groupTask gt
			LEFT JOIN gt.intendedGroup ug
			WHERE ug.id = :userGroupId
			GROUP BY ug.id, ug.name
			""")
	// AUDIT-FIX: статистика отдела по intendedGroup
	Optional<DepartmentTaskStatsDTO> findGroupUserStatsByGroup(@Param("userGroupId") Integer userGroupId);

	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			JOIN FETCH gt.creatorGroup ug
			JOIN FETCH t.creator cre
			LEFT JOIN FETCH t.assigner ass
			WHERE t.id = :id
			""")
	Optional<Task> findByIdWithDetails(@Param("id") Long id);

	// AUDIT-FIX: загрузка задачи с creator.apteka и assigner для оценки аптеки
	@Query("""
			SELECT t FROM Task t
			LEFT JOIN FETCH t.creator c
			LEFT JOIN FETCH c.apteka
			LEFT JOIN FETCH t.assigner
			WHERE t.id = :id
			""")
	Optional<Task> findByIdForRating(@Param("id") Long id);

	@Query("""
			SELECT t FROM Task t JOIN FETCH t.pictures WHERE t.id = :id
			""")
	Optional<Task> fetchPictures(@Param("id") Long id);

	@Query("""
			    SELECT t FROM Task t
			    LEFT JOIN FETCH t.employeeComments ec
			    LEFT JOIN FETCH ec.account acc
			    LEFT JOIN FETCH acc.client
				LEFT JOIN FETCH acc.apteka
			    LEFT JOIN FETCH acc.userGroup
			    WHERE t.id = :id
			""")
	Optional<Task> fetchCommentsForTask(@Param("id") Long id);

	@Query("""
			SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END
			FROM Task t
			WHERE (t.assigner.id = :accountId AND t.status IN :statusCollection)
				OR (t.creator.id = :accountId AND t.status IN :statusCollection)
			""")
	boolean existsByAccountIdAndStatus(@Param("accountId") UUID accountId, @Param("statusCollection") Set<TaskStatus> statusCollection);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			JOIN wt.groupTask gt
			WHERE gt.id = :groupTaskId
			AND t.status IN :activeStatuses
			""")
	boolean existsByGroupTaskAndStatusActive(@Param("groupTaskId") Integer groupTaskId, @Param("activeStatuses") Set<TaskStatus> activeStatuses);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			JOIN wt.groupTask gt
			WHERE gt.id = :groupTaskId
			AND t.status IN :nonActiveStatuses
			""")
	boolean existsByGroupTaskAndStatusNonActive(@Param("groupTaskId") Integer groupTaskId, @Param("nonActiveStatuses") Set<TaskStatus> nonActiveStatuses);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			WHERE wt.id = :workTypeId
			AND t.status IN :activeStatuses
			""")
	boolean existsByWorkTypeAndStatusActive(@Param("workTypeId") Integer workTypeId, @Param("activeStatuses") Set<TaskStatus> activeStatuses);

	@Query("""
			SELECT 1 FROM Task t
			JOIN t.workType wt
			WHERE wt.id = :workTypeId
			AND t.status IN :nonActiveStatuses
			""")
	boolean existsByWorkTypeAndStatusNonActive(@Param("workTypeId") Integer workTypeId, @Param("nonActiveStatuses") Set<TaskStatus> nonActiveStatuses);

	// AUDIT-FIX: задачи отдела для ежедневного отчёта (очередь intendedGroup + назначенные сотрудникам группы)
	@Query("""
			SELECT DISTINCT t FROM Task t
			JOIN FETCH t.workType w
			JOIN FETCH w.groupTask gt
			LEFT JOIN FETCH t.assigner ass
			LEFT JOIN FETCH ass.client
			LEFT JOIN FETCH ass.userGroup
			LEFT JOIN FETCH t.creator cre
			LEFT JOIN FETCH cre.client
			WHERE (
			    (t.assigner IS NULL AND gt.intendedGroup.id = :groupId)
			    OR (ass.userGroup.id = :groupId)
			)
			AND (
			    (t.creationDate >= :start AND t.creationDate < :end)
			    OR (t.closingDate >= :start AND t.closingDate < :end)
			    OR (t.updatedDate >= :start AND t.updatedDate < :end AND t.status = com.apteka.portal.models.TaskStatus.DENIED)
			    OR (
			        t.status IN (com.apteka.portal.models.TaskStatus.OPEN, com.apteka.portal.models.TaskStatus.PROCESSED)
			        AND t.creationDate < :end
			        AND (t.closingDate IS NULL OR t.closingDate >= :end)
			    )
			)
			ORDER BY t.creationDate ASC
			""")
	List<Task> findDepartmentTasksForDailyReport(
			@Param("groupId") Integer groupId,
			@Param("start") java.time.Instant start,
			@Param("end") java.time.Instant end);
}
