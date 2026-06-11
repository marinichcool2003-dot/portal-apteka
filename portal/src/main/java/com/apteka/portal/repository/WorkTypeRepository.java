package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.WorkType;

import java.util.List;
import java.util.Optional;


public interface WorkTypeRepository extends JpaRepository<WorkType, Integer>{
    Optional<WorkType> findByName(String name);

    boolean existsByNameAndGroupTaskId(String name, Integer groupTaskId);

    boolean existsByGroupTaskId(Integer groupTaskId);

    List<WorkType> findByGroupTaskId(Integer groupTaskId);
}
