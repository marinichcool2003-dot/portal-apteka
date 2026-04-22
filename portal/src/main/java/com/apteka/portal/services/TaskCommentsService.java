package com.apteka.portal.services;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.InvalidTaskCommentException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskComments;
import com.apteka.portal.repository.TaskCommentsInterface;
import com.apteka.portal.repository.TaskInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentsService {
    private final AptekaService aptekaService;
    private final TaskCommentsInterface taskCommentsInterface;
    private final TaskInterface taskInterface;
    private final ClientService clientService;

    @Transactional(readOnly = true)
    public List<TaskComments> getAll() {
        return taskCommentsInterface.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaskComments> getByTask(Long taskId) {
        if(!taskInterface.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        return taskCommentsInterface.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public TaskComments getOne(Long id) {
        return taskCommentsInterface.findById(id)
                .orElseThrow(() -> new TaskCommentNotFoundException(id));
    }

    @Transactional
    public TaskComments create(String comment, Long taskId, UUID clientId, Integer aptekaId) {
        Task task = taskInterface.findById(taskId)
            .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskComments.TaskCommentsBuilder builder = TaskComments.builder().comment(comment).task(task);
        if (clientId != null) {
            Client client = clientService.getOne(clientId);
            builder.client(client);
        }
        else if (aptekaId != null){
            Apteka apteka = aptekaService.getOne(aptekaId);
            builder.apteka(apteka);
        }
        else {
            throw new AvtorCommentNotInputException("Автор комментария не был указан или не существует.");
        }
        return taskCommentsInterface.save(builder.build());
    }

    @Transactional
    public TaskComments update(Long id, String comment) {
        TaskComments taskComment = getOne(id);
        if (comment == null || comment.isBlank()) {
            throw new InvalidTaskCommentException();
        }
        taskComment.setComment(comment.strip());
        return taskCommentsInterface.save(taskComment);
    }
}
