package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.WorkType;
import java.util.Optional;


@Repository
public interface WorkTypeRepository extends JpaRepository<WorkType, Integer>{
    Optional<WorkType> findByName(String name);

    boolean existsByNameAndGroupTaskId(String name, Integer groupTaskId);
}
