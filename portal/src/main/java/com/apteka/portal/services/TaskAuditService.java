package com.apteka.portal.services;

import org.springframework.stereotype.Service;

import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.UsersInApp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskAuditService {
    private final TaskCommentsService taskCommentsService;

    public String getAuthor(UsersInApp currentuser) {
        String authorName = "Система";
        if (currentuser instanceof Client client) {
            return client.getFullName();
        }
        if (currentuser instanceof Apteka apteka) {
            return apteka.getUserGroup().getName() + " " + apteka.getNumber();
        }
        return authorName;
    }

    public void addComment(String template, UsersInApp user, Task task) {
        String text = template.formatted(getAuthor(user), task.getId());
        taskCommentsService.create(text, task.getId(), user);
    }
}
