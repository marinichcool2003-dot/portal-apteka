package com.apteka.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.apteka.portal.models.TaskComment;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {
    @EntityGraph(attributePaths = {"task"})
    List<TaskComment> findByTaskId(Long taskId);
}
