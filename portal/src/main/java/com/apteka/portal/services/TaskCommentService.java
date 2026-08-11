package com.apteka.portal.services;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.mail.EmailContent;
import com.apteka.portal.util.HtmlUtils;
import com.apteka.portal.dtos.request.TaskCommentRequestDTO;
import com.apteka.portal.components.SseAfterCommitPublisher;
import com.apteka.portal.components.servicesecurity.TaskSecurityService;
import com.apteka.portal.dtos.response.TaskCommentResponseDTO;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.NotificationEventType;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.TaskCommentRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskCommentService {
    private final SseAfterCommitPublisher sseAfterCommitPublisher;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailTemplateService emailTemplateService;
    private final TaskCommentRepository taskCommentsRepository;
    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;
    private final TaskSecurityService taskSecurityService;

    @Transactional(readOnly = true)
    public List<TaskCommentResponseDTO> getByTask(Long taskId, AppUserDetails currentUser) {
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new TaskNotFoundException(taskId));
        taskSecurityService.canSelectTask(task, currentUser);
        return taskCommentsRepository.findByTaskId(taskId).stream()
                .map(TaskCommentResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskCommentResponseDTO getOne(Long id, AppUserDetails currentUser) {
        TaskComment comment = taskCommentsRepository.findById(id)
                .orElseThrow(() -> new TaskCommentNotFoundException(id));
        taskSecurityService.canSelectTask(comment.getTask(), currentUser);
        return TaskCommentResponseDTO.from(comment);
    }

    @Transactional
    public TaskCommentResponseDTO create(TaskCommentRequestDTO dto, AppUserDetails currentUser) {
        Task task = taskRepository.findById(dto.taskId())
                .orElseThrow(() -> new TaskNotFoundException(dto.taskId()));
        taskSecurityService.canSelectTask(task, currentUser);

        var builder = TaskComment.builder()
                .comment(dto.commentText().strip())
                .task(task);

        setCommentAuthor(builder, currentUser);

        TaskComment savedComment = taskCommentsRepository.save(builder.build());
        var signal = new SseEventNames.EntityUpdateSignalDTO(dto.taskId(), SseSignalTypes.UPDATED);
        Runnable ssePublish = () -> sseAfterCommitPublisher.publishAfterCommit(SseEventNames.REFRESH_TASKS, signal);

        Account commenter = savedComment.getAccount();
        String subject = "Комментарий к задаче: " + task.getTitle();
        String details = emailTemplateService.taskDetailsHtml(
                task.getId(), task.getTitle(), task.getStatus().getName())
                + "<p style=\"margin:12px 0 0;color:#333333;font-size:14px;line-height:1.6;\"><strong>Комментарий:</strong> "
                + HtmlUtils.escapeHtml(savedComment.getComment()).replace("\n", "<br/>")
                + "</p>";
        EmailContent content = emailTemplateService.renderNotification(
                subject, "Новый комментарий к задаче", details);

        notifyTaskCommentRecipients(task, commenter, content, ssePublish);

        return TaskCommentResponseDTO.from(savedComment);
    }

    private void notifyTaskCommentRecipients(Task task, Account commenter, EmailContent content,
            Runnable ssePublish) {
        boolean notified = false;
        if (task.getAssigner() != null && !task.getAssigner().getId().equals(commenter.getId())) {
            notificationDispatcher.dispatchToUser(
                    task.getAssigner().getId(), NotificationEventType.TASK_COMMENT, content, ssePublish);
            notified = true;
        }
        if (task.getCreator() != null && !task.getCreator().getId().equals(commenter.getId())
                && (task.getAssigner() == null || !task.getCreator().getId().equals(task.getAssigner().getId()))) {
            notificationDispatcher.dispatchToUser(
                    task.getCreator().getId(), NotificationEventType.TASK_COMMENT, content,
                    notified ? null : ssePublish);
            notified = true;
        }
        if (!notified) {
            sseAfterCommitPublisher.publishAfterCommit(SseEventNames.REFRESH_TASKS,
                    new SseEventNames.EntityUpdateSignalDTO(task.getId(), SseSignalTypes.UPDATED));
        }
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
        sseAfterCommitPublisher.publishAfterCommit(SseEventNames.REFRESH_TASKS, signal);
    }

    private void setCommentAuthor(TaskComment.TaskCommentBuilder builder, AppUserDetails currentUser) {
        Account account = accountRepository.findById(currentUser.getInternalId()).orElseThrow(
                () -> new AccessDeniedException("Пользователь не обнаружен!"));

        builder.account(account);
    }
}
