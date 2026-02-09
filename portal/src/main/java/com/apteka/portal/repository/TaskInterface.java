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

        @Query("SELECT t FROM Task t WHERE t.assignedApteka.id = :assignedAptekaId")
        List<Task> findByAssignedAptekaId(@Param("assignedAptekaId") Integer assignedAptekaId);

        @Query("SELECT t FROM Task t WHERE t.assignedClient.id = :assignedClientId")
        List<Task> findByAssignedClientId(@Param("assignedClientId") UUID assignedClientId);

        @Query("SELECT t FROM Task t WHERE t.createdByClient.id = :createdByClientId")
        List<Task> findByCreatedByClient(@Param("createdByClientId") UUID createdByClientId);

        @Query("SELECT t FROM Task t WHERE t.createdByApteka.id = :createdByAptekaId")
        List<Task> findByCreatedByApteka(@Param("createdByAptekaId") Integer createdByAptekaId);

        @Query("""
                            SELECT t FROM Task t
                            JOIN t.workType w
                            JOIN w.groupTask g
                            WHERE g.id = :groupId
                        """)
        List<Task> findByGroupId(@Param("groupId") Integer groupId);

        // @Query("""
        // SELECT t FROM Task t
        // WHERE (:clientId IS NULL OR t.assignedClient.id = :assignedClientId)
        // AND (:assignedAptekaId IS NULL OR t.assignedApteka.id = :assignedAptekaId)
        // AND (:createdByClientId IS NULL OR t.createdByClient.id = :createdByClientId)
        // AND (:workTypeId IS NULL OR t.workType.id = :workTypeId)
        // AND (:status IS NULL OR t.status = :status)
        // AND (:priority IS NULL OR t.priority = :priority)
        // AND (:fromDate IS NULL OR t.createdDate >= :fromDate)
        // AND (:toDate IS NULL OR t.createdDate <= :toDate)
        // """)

        // List<Task> filter(
        // @Param("assignedClientId") UUID assignedClientId,
        // @Param("assignedAptekaId") Integer assignedAptekaId,
        // @Param("createdByClientId") UUID createdByClientId,
        // @Param("workTypeId") Integer workTypeId,
        // @Param("status") TaskStatus status,
        // @Param("priority") TaskPriority priority,
        // @Param("fromDate") Date fromDate,
        // @Param("toDate") Date toDate);

        // @Query("""
        // SELECT t FROM Task t
        // JOIN t.assignedClient c
        // JOIN c.groupClient g
        // WHERE g.id =:groupClientId
        // """)

        @Query("""
                            SELECT t FROM Task t
                            WHERE t.assignedGroupClient.id = :groupClientId
                              AND t.status = :status
                        """)
        List<Task> findByGroupClient(
                        @Param("groupClientId") Integer groupClientId,
                        @Param("status") TaskStatus status);

        @Query("""
                            SELECT t FROM Task t
                            JOIN t.assignedApteka a
                            WHERE a.groupApteki.id = :groupAptekiId
                              AND t.status = :status
                        """)
        List<Task> findByGroupApteka(
                        @Param("groupAptekiId") Integer groupAptekiId,
                        @Param("status") TaskStatus status);

        @Query("""
                            SELECT COUNT(t)
                            FROM Task t
                            JOIN t.assignedGroupClient g
                            WHERE g.id = :groupClientId
                              AND t.status = :status
                        """)
        Long taskCountByGroup(
                        @Param("groupClientId") Integer groupClientId,
                        @Param("status") TaskStatus status);

        @Query("""
                            SELECT COUNT(t)
                            FROM Task t
                            JOIN t.assignedClient c
                            WHERE c.id = :clientId
                              AND t.status = :status
                        """)
        Long taskCountByClient(
                        @Param("clientId") UUID clientId,
                        @Param("status") TaskStatus status);
}
