package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.GroupGroupVisibility;


public interface GroupGroupVisibilityRepository extends JpaRepository<GroupGroupVisibility, Integer> {
    @Query("""
            SELECT CASE WHEN COUNT(gr) > 0 THEN true ELSE false END
            FROM GroupRelation gr
            WHERE (gr.firstGroupId = :firstId AND gr.secondGroupId = :secondId)
               OR (gr.firstGroupId = :secondId AND gr.secondGroupId = :firstId)
            """)
    boolean existsRelationBidirectional(@Param("firstId") Integer firstGroupId,
            @Param("secondId") Integer secondGroupId);
}
