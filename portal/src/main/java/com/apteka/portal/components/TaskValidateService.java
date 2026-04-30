package com.apteka.portal.components;

import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.services.SecurityUtils;
import com.apteka.portal.services.TaskService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskValidateService {

    private final TaskSecurityService taskSecurityService;
    private final TaskService taskService;

    public Task createTask(TaskRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getCurrentUser();
        validateTitle(dto.title());
        validateDescription(dto.description());
        taskSecurityService.validateCanCreate(dto, currentUser);
        return taskService.saveTask(dto, currentUser);
    }

    public void validateTitle(String title) {
        if (title == null || title.isBlank())
            throw new InvalidTaskTitleException();
    }

    public void validateDescription(String description) {
        if (description == null || description.isBlank())
            throw new InvalidTaskDescriptionException();
    }
}
