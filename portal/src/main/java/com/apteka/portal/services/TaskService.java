package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.exceptions.ClientBelongsToAnotherGroupException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.WorkType;
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
    private final TaskCommentsService taskCommentsService;
    private final TaskSecurityService taskSecurityService;


    // ==================================================
    // GET
    // ==================================================

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getAll() {
        log.info("Получение всех задач | поток {}", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(taskRepository.findAll());
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
    public CompletableFuture<List<Task>> getDepartamentTaskWithFilters(
            Integer groupId,
            UUID creatorClientId,
            Integer creatorAptekaId,
            UUID specificClientId,
            Integer specificAptekaId,
            TaskStatus status,
            TaskPriority priority,
            Integer groupTaskId) {
        List<Task> tasks = taskRepository.findDepartmentTasksWithFilters(
                groupId,
                creatorClientId,
                creatorAptekaId,
                specificClientId,
                specificAptekaId,
                status,
                priority,
                groupTaskId);
        return CompletableFuture.completedFuture(tasks);
    }

    // ==================================================
    // CREATE //Нужно добавить кастомную анотацию чтобы не было куча исполнителей и создателей
    // ==================================================

    @Transactional
    public Task createTask(TaskCreateRequestDTO dto, Object currentUser) {
        validate(dto.title(), dto.description());

        taskSecurityService.validateCanCreate(dto, currentUser);

        Task task = Task.builder()
                .title(dto.title())
                .description(dto.description())
                .comments(dto.comments() != null ? dto.comments().strip() : null)
                .workType(workTypeService.getOne(dto.workTypeId()))
                .build();

        if (dto.createdByAptekaId() != null) {
            Apteka apteka = aptekaService.getOne(dto.createdByAptekaId());
            task.setCreatedByApteka(apteka);
        } else if (dto.createdByClientId() != null) {
            Client client = clientService.getOne(dto.createdByClientId());
            task.setCreatedByClient(client);
        }

        setAssignee(task, dto);

        return taskRepository.save(task);

    }

    private void setAssignee(Task task, TaskCreateRequestDTO dto) {
        if (dto.assignedGroupId() != null) {
            task.setAssignedGroup(userGroupService.getOne(dto.assignedGroupId()));
        } else if (dto.assignedClientId() != null) {
            task.setAssignedClient(clientService.getOne(dto.assignedClientId()));
        } else if (dto.assignedAptekaId() != null) {
            task.setAssignedApteka(aptekaService.getOne(dto.assignedAptekaId()));
        }
    }

    // ==================================================
    // Создание задачи сотрудником для сотрудника только внутри группы
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> createByClientToClientInGroup(
            String title,
            String description,
            String comments,
            UUID creatorClient,
            UUID assignedClient,
            Integer workTypeId) {

        validate(title, description);

        Client creator = clientService.getOne(creatorClient);
        Client assigner = clientService.getOne(assignedClient);
        WorkType workType = workTypeService.getOne(workTypeId);

        if (!creator.getGroupClient().getName().equals(assigner.getGroupClient().getName())) {
            throw new ClientBelongsToAnotherGroupException(assigner.getUsername());
        }

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .creationDate(LocalDateTime.now())
                .status(TaskStatus.OPEN)
                .createdByClient(creator)
                .assignedClient(assigner)
                .workType(workType)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // STATUS
    // ==================================================
    @Transactional
    public Task changeStatus(Long id, String statusDescription, UUID clientId, Integer aptekaId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus != task.getStatus()) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(newStatus);

            if (newStatus == TaskStatus.CLOSED) {
                task.setClosingDate(LocalDateTime.now());
            }
            task.setUpdatedDate(LocalDateTime.now());

            String authorName = "Система";
            if (clientId != null) {
                authorName = clientService.getOne(clientId).getFullName();
            } else if (aptekaId != null) {
                authorName = aptekaService.getOne(aptekaId).getUserGroup().getName() + " "
                        + aptekaService.getOne(aptekaId).getNumber();
            }

            String commentText = "Пользователь %s изменил статус задачи #%d изменен c '%s' на '%s' "
                    .formatted(authorName, id, oldStatus, newStatus);

            Task savedTask = taskRepository.save(task);
            taskCommentsService.create(commentText, id, clientId, aptekaId);
            return savedTask;
        }

        return task;
    }

    // ==================================================
    // UPDATE / DELETE
    // ==================================================

    // ==================================================
    // Обновление содержания задачи (Заголовок, Описание)
    // ==================================================
    @Transactional
    public Task update(
            Long id,
            String title,
            String description,
            String comments) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        validate(title, description);

        if ((task.getStatus() == TaskStatus.DENIED) || (task.getStatus() == TaskStatus.CLOSED
                && LocalDateTime.now().isAfter(task.getClosingDate().plusMonths(1)))) {
            throw new BlockChangeIfNotActuallyTaskException();
        }

        if (comments != null)
            task.setComments(comments.strip());

        task.setUpdatedDate(new Date());

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // Распределение задачи на сотрудника для сотрудников из одной группы
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> changeAssignedClientInGroup(Long id, UUID assignedClientId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        Client assignedClient = clientService.getOne(assignedClientId);

        // Проверка, что сотрудник из аптеки на которую назаначена задача
        if (assignedClient.getGroupClient().getName() != task.getAssignedGroupClient().getName()) {
            throw new ClientBelongsToAnotherGroupException(assignedClient.getUsername());
        }

        // Распределение на сотрудника
        task.setAssignedClient(assignedClient);
        // Перестаёт принадлежать группе
        task.setAssignedGroupClient(null);

        task.setUpdatedDate(new Date());

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // Распределение задачи на любого сотрудника
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> changeAssignedClient(Long id, UUID assignedClientId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        Client assignedClient = clientService.getOne(assignedClientId);

        // Распределение на сотрудника
        task.setAssignedClient(assignedClient);
        // Перестаёт принадлежать группе
        task.setAssignedGroupClient(null);

        task.setUpdatedDate(new Date());

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // Распределение задачи на группу
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> changeAssignedGroupClient(Long id, Integer assignedGroupClientId) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        UserGroup assignedGroupClient = groupClientService.getOne(assignedGroupClientId);

        // Распределение на группу
        task.setAssignedGroupClient(assignedGroupClient);

        task.setUpdatedDate(new Date());

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
