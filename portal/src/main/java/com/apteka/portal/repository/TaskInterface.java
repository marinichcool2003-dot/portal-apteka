package com.apteka.portal.repository;

import java.util.Date;
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

        List<Task> findByAssignedAptekaId(Integer assignedAptekaId);

        List<Task> findByAssignedClientId(UUID assignedClientId);

        List<Task> findByCreatedByClient(UUID createdByClientId);

        List<Task> findByCreatedByApteka(Integer createdByAptekaId);

        @Query("""
                        SELECT t FROM Task t
                        JOIN t.workTask w
                        JOIN w.groupTask g
                        WHERE g.id =: groupId
                """)
        List<Task> findByGroupId(Integer groupId);

        @Query("""
                        SELECT t FROM Task t
                        WHERE (:clientId IS NULL OR t.client.id = :clientId)
                        AND (:aptekaId IS NULL OR t.apteka.id = :aptekaId)
                        AND (:createdByClientId IS NULL OR t.createdByClient.id = :createdByClientId)
                        AND (:workTaskId IS NULL OR t.workTask.id = :workTaskId)
                        AND (:status IS NULL OR t.status = :status)
                        AND (:priority IS NULL OR t.priority = :priority)
                        AND (:fromDate IS NULL OR t.date >= :fromDate)
                        AND (:toDate IS NULL OR t.date <= :toDate)
                """)

        List<Task> filter(
                        @Param("clientId") UUID clientId,
                        @Param("aptekaId") Integer aptekaId,
                        @Param("createdByClientId") UUID createdByClientId,
                        @Param("workTaskId") Integer workTaskId,
                        @Param("status") TaskStatus status,
                        @Param("priority") TaskPriority priority,
                        @Param("fromDate") Date fromDate,
                        @Param("toDate") Date toDate);

        @Query("""
                        SELECT t FROM Task t
                        JOIN t.assignedClient c
                        JOIN c.groupClient g
                        WHERE g.id =:groupClientId
                """)
                
        List<Task> findByGroupClient(@Param("groupId") Integer groupClientId,
                        @Param("status") TaskStatus status);


        @Query("""
                        SELECT t FROM Task t
                        JOIN t.assignedApteka a
                        JOIN a.groupApteki g
                        WHERE g.id =:groupAptekaId
                        """)
        List<Task> findByGroupApteka(@Param("groupId") Integer groupAptekaId,
                        @Param("status") TaskStatus status);
}
