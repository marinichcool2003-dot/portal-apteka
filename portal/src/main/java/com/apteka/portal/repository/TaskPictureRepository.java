package com.apteka.portal.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.TaskPicture;
import java.util.Optional;


@Repository
public interface TaskPictureRepository extends JpaRepository<TaskPicture, Long> {
    @EntityGraph(attributePaths = {"task"})
    Optional<TaskPicture> findById(Long id);
}
