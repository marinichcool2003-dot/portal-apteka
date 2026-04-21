package com.apteka.portal.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;

@Repository
public interface TaskInterface extends JpaRepository<Task, Long> {

  @Query("""
          SELECT DISTINCT t FROM Task t
          JOIN FETCH t.workType w
          JOIN FETCH w.groupTask g
          WHERE (
              t.assignedClient.id = :clientId
              OR t.createdByClient.id = :clientId
              OR (t.assignedGroupClient = :groupId)
          )
          AND (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:groupTaskId IS NULL OR g.id = :groupTaskId)
          ORDER BY t.creationDate DESC
      """)
  List<Task> findTasksForClient(
      @Param("clientId") UUID clientId,
      @Param("groupId") Integer groupId,
      @Param("status") TaskStatus status,
      @Param("priority") TaskPriority priority,
      @Param("groupTaskId") Integer groupTaskId);

  @Query("""
          SELECT DISTINCT t FROM Task t
          JOIN FETCH t.workType w
          JOIN FETCH w.groupTask g
          WHERE (
              t.assignedApteka.id = :aptekaId
              OR t.createdByApteka.id = :aptekaId
              OR (t.assignedApteka = :groupAptekiId)
          )
          AND (:status IS NULL OR t.status = :status)
          AND (:priority IS NULL OR t.priority = :priority)
          AND (:groupTaskId IS NULL OR g.id = :groupTaskId)
          ORDER BY t.creationDate DESC
      """)
  List<Task> findTasksForApteka(
      @Param("aptekaId") Integer aptekaId,
      @Param("groupId") Integer groupId,
      @Param("status") TaskStatus status,
      @Param("priority") TaskPriority priority,
      @Param("groupTaskId") Integer groupTaskId);

  @Query("""
          SELECT DISTINCT t FROM Task t
          JOIN FETCH t.workType w
          JOIN FETCH w.groupTask g
          WHERE t.assignedGroupClient.id = :groupId
            AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (:groupTaskId IS NULL OR g.id = :groupTaskId)
          ORDER BY t.creationDate DESC
      """)
  List<Task> findAllTasksByGroupClient(
      @Param("groupId") Integer groupId,
      @Param("status") TaskStatus status,
      @Param("priority") TaskPriority priority,
      @Param("groupTaskId") Integer groupTaskId);

  @Query("""
      SELECT DISTINCT t FROM Task t
      JOIN FETCH t.workType w
      JOIN FETCH w.groupTask g
      WHERE t.assignedGroupApteki.id =: groupId
        AND (:status IS NULL OR t.status = :status)
            AND (:priority IS NULL OR t.priority = :priority)
            AND (:groupTaskId IS NULL OR g.id = :groupTaskId)
          ORDER BY t.creationDate DESC
      """)
  List<Task> findAllTasksByGroupApteka(
      @Param("groupId") Integer groupId,
      @Param("status") TaskStatus status,
      @Param("priority") TaskPriority priority,
      @Param("groupTaskId") Integer groupTaskId);
}
