package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.TaskSecurityService;
import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskRepository taskRepository;
    private final AptekaService aptekaService;
    private final ClientService clientService;
    private final WorkTypeRepository workTypeRepository;
    private final TaskAuditService taskAuditService;
    private final TaskSecurityService taskSecurityService;

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getAll() {
        log.info("Получение всех задач | поток {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(taskRepository.findAll());
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<Task> getOne(Long id) {
        log.info("Получение задачи id={} | поток {}", id, Thread.currentThread().getName());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return CompletableFuture.completedFuture(task);
    }

    @Async("taskExecutor")
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getDepartamentTaskWithFilters(DepartamentTaskWithFiltersDTO dto) {
        List<Task> tasks = taskRepository.findDepartmentTasksWithFilters(
                dto.groupId(),
                dto.creatorClientId(),
                dto.creatorAptekaId(),
                dto.specificClientId(),
                dto.specificAptekaId(),
                dto.status(),
                dto.priority(),
                dto.groupTaskId());
        return CompletableFuture.completedFuture(tasks);
    }

    @Transactional
    public Task create(TaskRequestDTO dto) {
        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();

        taskSecurityService.validateCanCreate(dto, currentUser);
        validateTitle(dto.title());
        validateDescription(dto.description());

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .comments(dto.comments() != null ? dto.comments().strip() : null)
                .workType(workTypeRepository.getReferenceById(dto.workTypeId()))
                .build();

        switch (currentUser.getType()) {
            case APTEKA -> task.setCreatedByApteka(aptekaService.getOne(currentUser.getAptekaId()));
            case CLIENT -> task.setCreatedByClient(clientService.getOne(currentUser.getClientId()));
        }

        setAssignee(task, dto);

        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Long id, TaskRequestDTO dto) {

        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskSecurityService.validateCanUpdate(task, dto, currentUser);
        taskSecurityService.validateStatus(task, currentUser);

        if (dto.title() != null && !Objects.equals(task.getTitle(), dto.title())) {
            validateTitle(dto.title());
            taskAuditService.logChange(task, currentUser, "заголовок", task.getTitle(), dto.title());
        }

        if (dto.description() != null && !Objects.equals(task.getDescription(), dto.description())) {
            validateDescription(dto.description());
            taskAuditService.logChange(task, currentUser, "описание", task.getDescription(), dto.description());
            task.setDescription(dto.description());
        }

        if (taskSecurityService.changeAssigner(task, dto, currentUser)) {
            String oldAssigneName = getAssigneeName(task);
            setAssignee(task, dto);
            String newAssigneeName = getAssigneeName(task);

            taskAuditService.logChange(task, currentUser, "исполнителя", oldAssigneName, newAssigneeName);
        }

        if (dto.statusDescription() != null && !dto.statusDescription().isBlank()) {
            taskSecurityService.validateStatus(task, currentUser);
            task = changeStatus(task, dto.statusDescription(), currentUser);
        }

        if (dto.comments() != null && !dto.comments().isBlank()) {
            task.setComments(dto.comments().strip());
        }
        task.setUpdatedDate(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id) {

        AppUserDetails currentUser = SecurityUtils.getRequiredCurrentUser();

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (currentUser.hasRole(UserRole.ADMIN)) {
            taskRepository.delete(task);
            return;
        }
        throw new AccessDeniedException("Только пользователь с правами администратора может удалить задачу");
    }

    private Task changeStatus(Task task, String statusDescription, AppUserDetails currentUser) {

        taskSecurityService.validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus == task.getStatus())
            return task;
        TaskStatus oldStatus = task.getStatus();
        task.changeStatus(newStatus);

        String commentText = "Пользователь %s изменил статус задачи #%d изменен c '%s' на '%s' "
                .formatted(taskAuditService.getAuthor(currentUser), task.getId(), oldStatus, newStatus);

        taskAuditService.addComment(commentText, currentUser, task);

        return task;
    }

    private void setAssignee(Task task, TaskRequestDTO dto) {
        if (dto.assignedClientId() != null) {
            task.setAssignedClient(clientService.getOne(dto.assignedClientId()));
        } else if (dto.assignedAptekaId() != null) {
            task.setAssignedApteka(aptekaService.getOne(dto.assignedAptekaId()));
        }
    }

    private String getAssigneeName(Task task) {
        String assigneeName = task.getWorkType().getGroupTask().getUserGroup().getName();
        StringBuilder assigneeNameBuilder = new StringBuilder(assigneeName);
        if (task.getAssignedClient() != null) {
            assigneeNameBuilder.append(" - ").append(task.getAssignedClient().getFullName());
            return assigneeNameBuilder.toString();
        }
        if (task.getAssignedApteka() != null) {
            assigneeNameBuilder.append(" - ").append("Аптека №").append(task.getAssignedApteka().getNumber());
            return assigneeNameBuilder.toString();
        }
        return assigneeNameBuilder.append(" (Не назначен)").toString();
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank())
            throw new InvalidTaskTitleException();
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank())
            throw new InvalidTaskDescriptionException();
    }
}