package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.WorkTask;

@Repository
public interface WorkTaskInterface extends JpaRepository<WorkTask, Integer>{
    
}
