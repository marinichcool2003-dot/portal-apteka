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

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.ClientRole;
import com.apteka.portal.models.UsersInApp;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.repository.TaskInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskInterface taskRepository;
    private final AptekaService aptekaService;
    private final ClientService clientService;
    private final WorkTypeService workTypeService;
    private final UserGroupService userGroupService;
    private final TaskSecurityService taskSecurityService;
    private final TaskAuditService taskAuditService;

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getAll() {
        log.info("Получение всех задач | поток {}", Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(taskRepository::findAll);
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Task> getOne(Long id) {
        log.info("Получение задачи id={} | поток {}", id, Thread.currentThread().getName());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return CompletableFuture.completedFuture(task);
    }

    @Async
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
    public Task createTask(TaskRequestDTO dto, UsersInApp currentUser) {
        validateTitleAndDescripton(dto.title(), dto.description());

        taskSecurityService.validateCanCreate(dto, currentUser);

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .comments(dto.comments() != null ? dto.comments().strip() : null)
                .workType(workTypeService.getOne(dto.workTypeId()))
                .build();

        if (currentUser instanceof Apteka apteka) {
            Apteka currentApteka = aptekaService.getOne(apteka.getId());
            task.setCreatedByApteka(currentApteka);
        } else if (currentUser instanceof Client client) {
            Client currentClient = clientService.getOne(client.getId());
            task.setCreatedByClient(currentClient);
        }

        setAssignee(task, dto);

        Objects.requireNonNull(task, "Задача не может быть пустой");

        return taskRepository.save(task);
    }

    @Transactional
    public Task update(Long id, TaskRequestDTO dto, UsersInApp currentUser) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskSecurityService.validateCanUpdate(task, dto, currentUser);
        taskSecurityService.validateStatus(task, currentUser);

        if (!Objects.equals(task.getTitle(), dto.title())
                || !Objects.equals(task.getDescription(), dto.description())) {
            validateTitleAndDescripton(dto.title(), dto.description());
            String commentText = "Пользователь %s изменил описание задачи #%d"
                    .formatted(taskAuditService.getAuthor(currentUser), task.getId());

            taskAuditService.addComment(commentText, currentUser, task);
        }

        if (dto.statusDescription() != null && !dto.statusDescription().isBlank()) {
            task = changeStatus(task, dto.statusDescription(), currentUser);
        }

        if (dto.comments() != null && !dto.comments().isBlank()) {
            task.setComments(dto.comments().strip());
        }

        task.setUpdatedDate(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id, UsersInApp currentUser) {

        Objects.requireNonNull(id, "Идентификатор не был передан");

        Task task = taskRepository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));


        if (currentUser instanceof Client client && client.getRole() == ClientRole.ADMIN) {
            taskRepository.delete(task);
            return;
        }
        throw new AccessDeniedException("Только пользователь с правами администратора может удалить задачу");
    }

    private Task changeStatus(Task task, String statusDescription, UsersInApp currentUser) {

        taskSecurityService.validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus == task.getStatus()) return task;
        TaskStatus oldStatus = task.getStatus();
        task.changeStatus(newStatus);

        String commentText = "Пользователь %s изменил статус задачи #%d изменен c '%s' на '%s' "
                .formatted(taskAuditService.getAuthor(currentUser), task.getId(), oldStatus, newStatus);

        taskAuditService.addComment(commentText, currentUser, task);

        return task;
    }

    private void setAssignee(Task task, TaskRequestDTO dto) {
        if (dto.assignedGroupId() != null) {
            task.setAssignedGroup(userGroupService.getOne(dto.assignedGroupId()));
        } else if (dto.assignedClientId() != null) {
            task.setAssignedClient(clientService.getOne(dto.assignedClientId()));
        } else if (dto.assignedAptekaId() != null) {
            task.setAssignedApteka(aptekaService.getOne(dto.assignedAptekaId()));
        }
    }

    private void validateTitleAndDescripton(String title, String description) {
        if (title == null || title.isBlank())
            throw new InvalidTaskTitleException();

        if (description == null || description.isBlank())
            throw new InvalidTaskDescriptionException();
    }

}