package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.InvalidTaskCommentException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.models.TaskComments;
import com.apteka.portal.repository.TaskCommentsInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentsService {
    private final TaskCommentsInterface taskCommentsInterface;
    private final TaskService taskService;

    @Transactional(readOnly = true)
    public List<TaskComments> getAll() {
        return taskCommentsInterface.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaskComments> getByTask(Long taskId) {
        taskService.getOne(taskId);
        return taskCommentsInterface.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public TaskComments getOne(Long id) {
        return taskCommentsInterface.findById(id)
                .orElseThrow(() -> new TaskCommentNotFoundException(id));
    }

    @Transactional
    public TaskComments update(Long id, String comment) {
        TaskComments taskComment = getOne(id);
        comment = comment.strip();
        if (comment == null || comment.isEmpty()) {
            throw new InvalidTaskCommentException();
        }
        taskComment.setComment(comment);
        return taskCommentsInterface.save(taskComment);
    }
}
