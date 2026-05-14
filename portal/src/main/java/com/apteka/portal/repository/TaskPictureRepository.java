package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.TaskPicture;

@Repository
public interface TaskPictureRepository extends JpaRepository<TaskPicture, Long> {
    
}
