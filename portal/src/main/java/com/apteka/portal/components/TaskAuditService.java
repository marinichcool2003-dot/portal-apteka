package com.apteka.portal.components;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.services.TaskCommentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskCommentService taskCommentsService;

    public String getAuthor(AppUserDetails currentuser) {
        if (currentuser == null)
            return "Система";
        return currentuser.getDisplayName();
    }

    @Async("taskExecutor")
    public void logChange(Task task, AppUserDetails currentUser, String fieldName, Object oldValue, Object newValue) {
        log.info("Фоновое логирование изменения поля '{}' | Поток: {}", fieldName, Thread.currentThread().getName());

        String text = "Пользователь %s изменил %s задачи #%d. Старое значение: [%s], Новое значение: [%s]"
                .formatted(getAuthor(currentUser), fieldName, task.getId(),
                        oldValue != null ? oldValue.toString().strip() : "пусто",
                        newValue != null ? newValue.toString().strip() : "пусто");

        taskCommentsService.create(text, task.getId(), currentUser);
    }

    @Async("taskExecutor")
    public void addComment(String readyText, AppUserDetails user, Task task) {
        log.info("Фоновое добавление системного комментария | Поток: {}", Thread.currentThread().getName());

        taskCommentsService.create(readyText, task.getId(), user);
    }
}
