package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.apteka.portal.models.WorkType;

import java.util.List;
import java.util.Optional;

public interface WorkTypeRepository extends JpaRepository<WorkType, Integer> {
    boolean existsByNameAndGroupTaskId(String name, Integer groupTaskId);

    @Query("""
            SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END
            FROM WorkType w
            JOIN w.groupTask gt
            JOIN gt.creatorGroup cg
            JOIN gt.intendedGroup ig
            WHERE gt.id = :groupTaskId 
            AND w.isActive = true
            AND gt.isActive = true 
            AND cg.isActive = true 
            AND ig.isActive = true
            """)
    boolean existsByGroupTaskIdActive(@Param("groupTaskId") Integer groupTaskId);

    @Query("""
            SELECT w FROM WorkType w
            JOIN FETCH w.groupTask gt
            JOIN FETCH gt.creatorGroup ug
            WHERE w.id = :id
            """)
    Optional<WorkType> findByIdWithGroupTaskAndCreatorGroup(Integer id);

    @Query("""
            SELECT w FROM WorkType w
            JOIN FETCH w.groupTask gt
            JOIN FETCH gt.creatorGroup cg
            JOIN FETCH gt.intendedGroup ig
            WHERE gt.id = :groupTaskId 
            AND (
                (:isActive = true AND w.isActive = true AND gt.isActive = true AND cg.isActive = true AND ig.isActive = true)
                OR
                (:isActive = false AND (w.isActive = false OR gt.isActive = false OR cg.isActive = false AND ig.isActive = false))
            )
            ORDER BY gt.id, w.id, w.name
            """)
    List<WorkType> findByGroupTaskIdAndIsActive(@Param("groupTaskId") Integer groupTaskId,
            @Param("isActive") Boolean isActive);
}
