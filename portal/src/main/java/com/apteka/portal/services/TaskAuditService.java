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

    public void addComment(String template, AppUserDetails user, Task task) {
        String text = template.formatted(getAuthor(user), task.getId());
        taskCommentsService.create(text, task.getId(), user);
    }
}
