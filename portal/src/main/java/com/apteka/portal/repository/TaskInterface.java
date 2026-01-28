package com.apteka.portal.repository;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;

@Repository
public interface TaskInterface extends JpaRepository<Task, Long> {

    List<Task> findByAptekaId(Integer aptekaId);

    List<Task> findByClientId(UUID clientId);

    @Query("""
                SELECT t FROM Task t
                JOIN t.work_task w
                JOIN w.groupId g
                WHERE g.id =: groupId
        """)
    List<Task> findByGroupId(Integer groupId);

    @Query("SELECT t FROM Task t WHERE t.createdByClient.id = :createdByClientId")
    List<Task> findByCreatedByClient(UUID createdByClientId);

    @Query("""
                SELECT t FROM Task t
                WHERE (:clientId IS NULL OR t.client.id = :clientId)
                AND (:createdByAptekaId IS NULL OR t.apteka.id = :createdByAptekaId)
                AND (:createdByClientId IS NULL OR t.createdByClient.id = :createdByClientId)
                AND (:workTaskId IS NULL OR t.workTask.id = :workTaskId)
                AND (:status IS NULL OR t.status = :status)
                AND (:priority IS NULL OR t.priority = :priority)
                AND (:fromDate IS NULL OR t.date >= :fromDate)
                AND (:toDate IS NULL OR t.date <= :toDate)
        """)
    List<Task> filter(
            UUID clientId,
            Integer aptekaId,
            Integer createdByAptekaId,
            UUID createdByClientId,
            Integer workTaskId,
            TaskStatus status,
            Date fromDate,
            Date toDate);

    @Query("""
                SELECT t FROM Task t
                JOIN t.client c
                JOIN c.group g
                WHERE g.id =:groupId AND t.status =: status
        """)
    List<Task> findByGroupClient(@Param("groupId") Integer groupId,
            @Param("status") TaskStatus status);
}
