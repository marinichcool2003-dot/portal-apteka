package com.apteka.portal.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.response.TaskCommentResponseDTO;
import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.TaskComment;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskCommentRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentService {

    private final AptekaRepository aptekaRepository;
    private final TaskCommentRepository taskCommentsRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;

    @Transactional(readOnly = true)
    public List<TaskCommentResponseDTO> getAll() {
        return taskCommentsRepository.findAll().stream()
                .map(TaskCommentResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskCommentResponseDTO> getByTask(Long taskId) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }
        return taskCommentsRepository.findByTaskId(taskId).stream()
                .map(TaskCommentResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskCommentResponseDTO getOne(Long id) {
        TaskComment comment = taskCommentsRepository.findById(id)
                .orElseThrow(() -> new TaskCommentNotFoundException(id));
        return TaskCommentResponseDTO.from(comment);
    }

    @Transactional
    public TaskCommentResponseDTO create(String commentText, Long taskId, AppUserDetails currentUser) {
        if (!taskRepository.existsById(taskId)) {
            throw new TaskNotFoundException(taskId);
        }

        var taskProxy = taskRepository.getReferenceById(taskId);

        var builder = TaskComment.builder()
                .comment(commentText.strip())
                .task(taskProxy);

        setCommentAuthor(builder, currentUser);

        TaskComment savedComment = taskCommentsRepository.save(builder.build());
        return TaskCommentResponseDTO.from(savedComment);
    }

    @Transactional
    public void delete(Long id, AppUserDetails currentUser) {
        if (!taskCommentsRepository.existsById(id)) {
            throw new TaskCommentNotFoundException(id);
        }
        taskCommentsRepository.deleteById(id);
    }

    private void setCommentAuthor(TaskComment.TaskCommentBuilder builder, AppUserDetails currentUser) {
        if (currentUser == null) {
            return;
        }

        if (currentUser.isClient()) {
            if (currentUser.getClientId() == null) {
                throw new AvtorCommentNotInputException("ID клиента отсутствует в контексте безопасности.");
            }
            builder.client(clientRepository.getReferenceById(currentUser.getClientId()));
        } else if (currentUser.isApteka()) {
            if (currentUser.getAptekaId() == null) {
                throw new AvtorCommentNotInputException("ID аптеки отсутствует в контексте безопасности.");
            }
            builder.apteka(aptekaRepository.getReferenceById(currentUser.getAptekaId()));
        } else {
            throw new AvtorCommentNotInputException("Автор комментария имеет неопределенный тип аккаунта.");
        }
    }
}
