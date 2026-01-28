package com.apteka.portal.services;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.TaskInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final TaskInterface taskRepository;
    private final AptekaService aptekaService;
    private final ClientService clientService;
    private final WorkTaskService workTaskService;
    private final GroupTaskService groupTaskService;
    private final GroupClientService groupClientService;

    // ==================================================
    // GET
    // ==================================================

    //===================================================
    // Получение всех задач
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getAll() {
        log.info("Получение всех задач | поток {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(taskRepository.findAll());
    }

    //===================================================
    // Получение одной задачи
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Task> getOne(Long id) {
        log.info("Получение задачи id={} | поток {}", id, Thread.currentThread().getName());

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        return CompletableFuture.completedFuture(task);
    }

    //===================================================
    // Получение задач назначенных аптеке
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByAssignedApteka(Integer assignedAptekaId) {
        aptekaService.getOne(assignedAptekaId);
        return CompletableFuture.completedFuture(
                taskRepository.findByAssignedAptekaId(assignedAptekaId));
    }

    //===================================================
    // Получение задач назначенных сотруднику
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByAssignedClient(UUID assignedClientId) {
        clientService.getOne(assignedClientId);
        return CompletableFuture.completedFuture(
                taskRepository.findByAssignedClientId(assignedClientId));
    }

    //===================================================
    // Получение задач созданных сотрудником
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByCreatedByClient(UUID createdByClientId) {
        clientService.getOne(createdByClientId);
        return CompletableFuture.completedFuture(
                taskRepository.findByCreatedByClient(createdByClientId));
    }

    //===================================================
    // Получение задач созданных аптекой
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByCreatedByApteka(Integer createdByAptekaId) {
        aptekaService.getOne(createdByAptekaId);
        return CompletableFuture.completedFuture(
                taskRepository.findByCreatedByApteka(createdByAptekaId));
    }

    //===================================================
    // Получение задач по группе
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByGroup(Integer groupId) {
        groupTaskService.getOne(groupId);
        return CompletableFuture.completedFuture(
                taskRepository.findByGroupId(groupId));
    }


    //===================================================
    // Получение задач по группе сотрудников
    //===================================================
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByGroupClient(Integer groupId, String statusDescription) {
        groupClientService.getOne(groupId);

        TaskStatus status = TaskStatus.fromDescription(statusDescription);

        return CompletableFuture.completedFuture(
                taskRepository.findByGroupClient(groupId, status));
    }

    // ==================================================
    // FILTER
    // ==================================================

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> filter(
            UUID clientId,
            Integer aptekaId,
            UUID createdByClientId,
            Integer workTaskId,
            TaskStatus status,
            TaskPriority priority,
            Date fromDate,
            Date toDate) {

        log.info("Фильтр задач | поток {}", Thread.currentThread().getName());

        return CompletableFuture.completedFuture(
                taskRepository.filter(
                        clientId,
                        aptekaId,
                        createdByClientId,
                        workTaskId,
                        status,
                        priority,
                        fromDate,
                        toDate));
    }

    // ==================================================
    // CREATE
    // ==================================================

    @Async
    @Transactional
    public CompletableFuture<Task> createByAptekaToClient(
            String title,
            String description,
            String comments,
            Integer aptekaId,
            Integer workTypeId,
            UUID clientId) {

        validate(title, description);

        Apteka apteka = aptekaService.getOne(aptekaId);
        WorkType workType = workTaskService.getOne(workTypeId);
        Client client = clientService.getOne(clientId);

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .date(new Date())
                .status(TaskStatus.OPEN)
                .createdByApteka(apteka)
                .assignedClient(client)
                .workType(workType)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    @Async
    @Transactional
    public CompletableFuture<Task> createByClientToClient(
            String title,
            String description,
            String comments,
            UUID creatorClient,
            UUID assignedClient,
            Integer workTaskId) {

        validate(title, description);

        Client creator = clientService.getOne(creatorClient);
        Client assigner = clientService.getOne(assignedClient);
        WorkType workTask = workTaskService.getOne(workTaskId);

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .date(new Date())
                .status(TaskStatus.OPEN)
                .createdByClient(client)
                .workTask(workTask)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // STATUS
    // ==================================================

    @Async
    @Transactional
    public CompletableFuture<Task> openTask(Long id) {
        return changeStatus(id, TaskStatus.OPEN);
    }

    @Async
    @Transactional
    public CompletableFuture<Task> processedTask(Long id) {
        return changeStatus(id, TaskStatus.PROCESSED);
    }

    @Async
    @Transactional
    public CompletableFuture<Task> closeTask(Long id) {
        return changeStatus(id, TaskStatus.CLOSED);
    }

    @Async
    @Transactional
    public CompletableFuture<Task> deniedTask(Long id) {
        return changeStatus(id, TaskStatus.DENIED);
    }

    private CompletableFuture<Task> changeStatus(Long id, TaskStatus status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task.setStatus(status);

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // UPDATE / DELETE
    // ==================================================

    @Async
    @Transactional
    public CompletableFuture<Task> update(
            Long id,
            String title,
            String description,
            String comments) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (title != null && !title.isBlank())
            task.setTitle(title.strip());

        if (description != null && !description.isBlank())
            task.setDescription(description.strip());

        if (comments != null)
            task.setComments(comments.strip());

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    @Async
    @Transactional
    public CompletableFuture<Void> delete(Long id) {

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        taskRepository.deleteById(id);

        return CompletableFuture.completedFuture(null);
    }

    private void validate(String title, String description) {
        if (title == null || title.isBlank())
            throw new InvalidTaskTitleException();

        if (description == null || description.isBlank())
            throw new InvalidTaskDescriptionException();
    }
}
