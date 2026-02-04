package com.apteka.portal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.apteka.portal.models.TaskComments;

@Repository
public interface TaskCommentsInterface extends JpaRepository<TaskComments, Long>{
    List<TaskComments> findByTaskId(Long taskId);
}
