package com.apteka.portal.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.MailOutbox;
import com.apteka.portal.models.MailOutboxStatus;

public interface MailOutboxRepository extends JpaRepository<MailOutbox, UUID> {

    @Query("""
            SELECT m FROM MailOutbox m
            WHERE m.status IN :statuses
              AND (
                    (m.status = com.apteka.portal.models.MailOutboxStatus.PENDING
                        AND (m.nextAttemptAt IS NULL OR m.nextAttemptAt <= :now))
                 OR (m.status = com.apteka.portal.models.MailOutboxStatus.FAILED
                        AND m.nextAttemptAt IS NOT NULL AND m.nextAttemptAt <= :now)
              )
            ORDER BY m.createdAt ASC
            """)
    List<MailOutbox> findReadyForRetry(
            @Param("statuses") List<MailOutboxStatus> statuses,
            @Param("now") Instant now);
}
