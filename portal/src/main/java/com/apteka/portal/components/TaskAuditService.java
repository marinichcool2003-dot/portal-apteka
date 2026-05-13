package com.apteka.portal.components;

import org.springframework.stereotype.Component;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.services.TaskCommentService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskCommentService taskCommentsService;

    public String getAuthor(AppUserDetails currentuser) {    
        if(currentuser == null) return "Система";
        return currentuser.getDisplayName();
    }

    public void logChange(Task task, AppUserDetails currentUser, String fieldName, Object oldValue, Object newValue) {
        String text = "Пользователь %s изменил %s задачи #%d. Старое значение: [%s], Новое значение: [%s]"
            .formatted(getAuthor(currentUser), fieldName, task.getId(), 
                oldValue != null ? oldValue : "пусто", 
                newValue != null ? newValue : "пусто");
        taskCommentsService.create(text, task.getId(), currentUser);
    }

    public void addComment(String template, AppUserDetails user, Task task) {
        String text = template.formatted(getAuthor(user), task.getId());
        taskCommentsService.create(text, task.getId(), user);
    }
}
