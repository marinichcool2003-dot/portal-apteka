package com.apteka.portal.components;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.TaskCommentRequestDTO;
import com.apteka.portal.exceptions.AsyncCommentException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.services.TaskCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskCommentService taskCommentsService;
    private final TaskRepository taskRepository;

    public String getAuthor(AppUserDetails currentuser) {
        if (currentuser == null)
            return "Система";
        return currentuser.getDisplayName();
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logChange(Long taskId, AppUserDetails currentUser, String fieldName, Object oldValue, Object newValue) {
        try {
            log.info("Фоновое логирование изменения поля '{}' | Поток: {}", fieldName,
                    Thread.currentThread().getName());

            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                log.error("Задача с id {} не найдена для записи лога", taskId);
                return;
            }

            String text = "Пользователь %s изменил %s задачи #%d. Старое значение: [%s], Новое значение: [%s]"
                    .formatted(getAuthor(currentUser), fieldName, taskId,
                            oldValue != null ? oldValue.toString().strip() : "пусто",
                            newValue != null ? newValue.toString().strip() : "пусто");

            taskCommentsService.create(TaskCommentRequestDTO.builder().commentText(text).taskId(task.getId()).build(),
                    currentUser);
        } catch (AsyncCommentException e) {
            log.error("Не удалось асинхронно создать комментарий");
        }
    }

    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addComment(String readyText, AppUserDetails user, Long taskId) {
        try {
            log.info("Фоновое добавление системного комментария | Поток: {}", Thread.currentThread().getName());

            Task task = taskRepository.findById(taskId).orElse(null);
            if (task == null) {
                log.error("Задача с id {} не найдена для записи лога", taskId);
                return;
            }

            taskCommentsService.create(
                    TaskCommentRequestDTO.builder().commentText(readyText).taskId(task.getId()).build(),
                    user);
        } catch (AsyncCommentException e) {
            log.error("Не удалось асинхронно создать комментарий");
        }

    }
}
