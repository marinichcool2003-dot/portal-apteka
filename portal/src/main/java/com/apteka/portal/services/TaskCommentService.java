package com.apteka.portal.services;

import com.apteka.portal.controllers.SseController;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.TaskCommentRequestDTO;
import com.apteka.portal.dtos.response.TaskCommentResponseDTO;
import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskCommentRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentService {
    private final SseController sseController;
    private final AptekaRepository aptekaRepository;
    private final TaskCommentRepository taskCommentsRepository;
    private final TaskRepository taskRepository;
    private final ClientRepository clientRepository;

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
    public TaskCommentResponseDTO create(TaskCommentRequestDTO dto, AppUserDetails currentUser) {
        if (!taskRepository.existsById(dto.taskId())) {
            throw new TaskNotFoundException(dto.taskId());
        }

        var taskProxy = taskRepository.getReferenceById(dto.taskId());

        var builder = TaskComment.builder()
                .comment(dto.commentText().strip())
                .task(taskProxy);

        setCommentAuthor(builder, currentUser);

        TaskComment savedComment = taskCommentsRepository.save(builder.build());
        var signal = new SseEventNames.EntityUpdateSignalDTO(dto.taskId(), SseSignalTypes.UPDATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, signal);
        return TaskCommentResponseDTO.from(savedComment);
    }

    @Transactional
    public void delete(Long id, AppUserDetails currentUser) {
        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только администратор может удалять комментарии");
        }
        TaskComment comment = taskCommentsRepository.findById(id)
            .orElseThrow(() -> new TaskCommentNotFoundException(id));

        taskCommentsRepository.delete(comment);
        var signal = new SseEventNames.EntityUpdateSignalDTO(comment.getTask().getId(), SseSignalTypes.UPDATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, signal);
    }

    private void setCommentAuthor(TaskComment.TaskCommentBuilder builder, AppUserDetails currentUser) {
        if (currentUser == null) {
            return;
        }

        if (currentUser.isClient()) {
            builder.client(clientRepository.getReferenceById(currentUser.getInternalId()));
        } else if (currentUser.isApteka()) {
            builder.apteka(aptekaRepository.getReferenceById(currentUser.getInternalId()));
        } else {
            throw new AvtorCommentNotInputException("Автор комментария имеет неопределенный тип аккаунта.");
        }
    }
}
