package com.apteka.portal.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.AptekaTaskRating;

public interface AptekaTaskRatingRepository extends JpaRepository<AptekaTaskRating, Long> {

    @EntityGraph(attributePaths = {"task", "apteka", "raterAccount", "raterAccount.client"})
    Optional<AptekaTaskRating> findByTaskId(Long taskId);

    boolean existsByTaskId(Long taskId);

    @EntityGraph(attributePaths = {"task", "apteka", "raterAccount", "raterAccount.client"})
    Page<AptekaTaskRating> findByAptekaId(UUID aptekaId, Pageable pageable);

    @Query("""
            SELECT COALESCE(AVG(r.stars), 0.0), COUNT(r)
            FROM AptekaTaskRating r
            WHERE r.apteka.id = :aptekaId
            """)
    List<Object[]> findAggregatesByAptekaId(@Param("aptekaId") UUID aptekaId);
}
