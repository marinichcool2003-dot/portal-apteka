package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskComments;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskCommentsRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentsService {
    private final AptekaRepository aptekaRepository;
    private final TaskCommentsRepository taskCommentsRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<TaskComments> getAll() {
        return taskCommentsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<TaskComments> getByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        return taskCommentsRepository.findByTaskId(taskId);
    }

    @Transactional(readOnly = true)
    public TaskComments getOne(Long id) {
        return taskCommentsRepository.findById(id)
                .orElseThrow(() -> new TaskCommentNotFoundException(id));
    }

    @Transactional
    public TaskComments create(String comment, Long taskId, AppUserDetails currentUser) {
        
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new TaskNotFoundException(taskId));

        TaskComments.TaskCommentsBuilder builder = TaskComments.builder()
                .comment(comment)
                .task(task);

        if (currentUser.isClient()) {
            builder.client(clientRepository.getReferenceById(currentUser.getClientId()));
        } else if (currentUser.isApteka()) {
            builder.apteka(aptekaRepository.getReferenceById(currentUser.getAptekaId()));
        } else {
            throw new AvtorCommentNotInputException("Автор комментария не был указан или не существует.");
        }

        return taskCommentsRepository.save(builder.build());
    }

    // @Transactional
    // public TaskComments update(Long id, String comment) {
    //     TaskComments taskComment = getOne(id);
    //     if (comment == null || comment.isBlank()) {
    //         throw new InvalidTaskCommentException();
    //     }
    //     taskComment.setComment(comment.strip());
    //     return taskCommentsRepository.save(taskComment);
    // }
}
