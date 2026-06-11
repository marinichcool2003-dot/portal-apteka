package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.TaskPicture;
import java.util.Optional;


public interface TaskPictureRepository extends JpaRepository<TaskPicture, Long> {
    @EntityGraph(attributePaths = {"task"})
    Optional<TaskPicture> findById(Long id);
}
