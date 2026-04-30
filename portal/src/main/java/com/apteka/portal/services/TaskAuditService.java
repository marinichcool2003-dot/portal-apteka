package com.apteka.portal.services;

import org.springframework.stereotype.Service;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskCommentsService taskCommentsService;

    public String getAuthor(AppUserDetails currentuser) {    
        if(currentuser == null) return "Система";
        return currentuser.getDisplayName();
    }

    public void logChange(Task task, AppUserDetails curentUser, String fieldName, Object oldValue, Object newValue) {
        String text = "Пользователь %s изменил %s задачи #%d. Старое значение: [%s], Новое значение: [%s]"
            .formatted(getAuthor(curentUser), fieldName, task.getId(), 
                oldValue != null ? oldValue : "пусто", 
                newValue != null ? newValue : "пусто");
        taskCommentsService.create(text, task.getId(), curentUser);
    }

    public void addComment(String template, AppUserDetails user, Task task) {
        String text = template.formatted(getAuthor(user), task.getId());
        taskCommentsService.create(text, task.getId(), user);
    }
}
