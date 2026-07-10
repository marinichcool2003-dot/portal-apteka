package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.apteka.portal.models.WorkType;

import java.util.List;
import java.util.Optional;


public interface WorkTypeRepository extends JpaRepository<WorkType, Integer>{
    Optional<WorkType> findByName(String name);

    boolean existsByNameAndGroupTaskId(String name, Integer groupTaskId);

    @Query("""
            SELECT 1 FROM WorkType w 
            WHERE w.groupTask.id = :groupTaskId
            AND w.isActive = true
            """)
    boolean existsByGroupTaskIdActive(Integer groupTaskId);

    List<WorkType> findByGroupTaskId(Integer groupTaskId);
}
