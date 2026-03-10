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

import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.exceptions.ClientBelongsToAnotherGroupException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupClient;
import com.apteka.portal.models.Task;
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
    private final WorkTypeService workTypeService;
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
    // Получение задач по группе сотрудников с определенным статусом
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
    // @Async
    // @Transactional(readOnly = true)
    // public CompletableFuture<List<Task>> filter(
    //         UUID clientId,
    //         Integer aptekaId,
    //         UUID createdByClientId,
    //         Integer workTaskId,
    //         TaskStatus status,
    //         TaskPriority priority,
    //         Date fromDate,
    //         Date toDate) {

    //     log.info("Фильтр задач | поток {}", Thread.currentThread().getName());

    //     return CompletableFuture.completedFuture(
    //             taskRepository.filter(
    //                     clientId,
    //                     aptekaId,
    //                     createdByClientId,
    //                     workTaskId,
    //                     status,
    //                     priority,
    //                     fromDate,
    //                     toDate));
    // }

    // ==================================================
    // CREATE
    // ==================================================

    // ==================================================
    // Создание задачи аптекой для сотрудника
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> createByAptekaToClient(
            String title,
            String description,
            String comments,
            Integer createByAptekaId,
            Integer workTypeId,
            UUID assignedClientId) {

        validate(title, description);

        Apteka apteka = aptekaService.getOne(createByAptekaId);
        WorkType workType = workTypeService.getOne(workTypeId);
        Client client = clientService.getOne(assignedClientId);

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .creationDate(new Date())
                .status(TaskStatus.OPEN)
                .createdByApteka(apteka)
                .assignedClient(client)
                .workType(workType)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
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

        if(!creator.getGroupClient().getName().equals(assigner.getGroupClient().getName())) {
            throw new ClientBelongsToAnotherGroupException(assigner.getUsername());
        }

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .creationDate(new Date())
                .status(TaskStatus.OPEN)
                .createdByClient(creator)
                .assignedClient(assigner)
                .workType(workType)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // Создание задачи сотрудником для сотрудника любой группы
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> createByClientToClient(
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

        Task task = Task.builder()
                .title(title.strip())
                .description(description.strip())
                .comments(comments != null ? comments.strip() : null)
                .creationDate(new Date())
                .status(TaskStatus.OPEN)
                .createdByClient(creator)
                .assignedClient(assigner)
                .workType(workType)
                .build();

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // Создание задачи аптекой для группы сотрудников
    // ==================================================
    @Async
    @Transactional
    public CompletableFuture<Task> createByAptekaToGroupClient(
        String title,
        String description,
        String comments,
        Integer createdAptekaId,
        Integer assignedGroupClientId,
        Integer workTypeId
    ) {
        validate(title, description);

        Apteka apteka = aptekaService.getOne(createdAptekaId);
        GroupClient froupClient = groupClientService.getOne(assignedGroupClientId);
        WorkType workType = workTypeService.getOne(workTypeId);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .comments(comments)
                .creationDate(new Date())
                .status(TaskStatus.OPEN)
                .createdByApteka(apteka)
                .assignedGroupClient(froupClient)
                .workType(workType)
                .build();
        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // STATUS
    // ==================================================
    public CompletableFuture<Task> changeStatus(Long id, String statusDescription) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        task.setStatus(newStatus);

        if (newStatus == TaskStatus.CLOSED) {
            task.setClosingDate(new Date());
        }

        return CompletableFuture.completedFuture(taskRepository.save(task));
    }

    // ==================================================
    // UPDATE / DELETE
    // ==================================================

    // ==================================================
    // Обновление содержания задачи (Заголовок, Описание)
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

        validate(title, description);

        if (task.getStatus() == TaskStatus.CLOSED || task.getStatus() == TaskStatus.DENIED) {
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

        //Проверка, что сотрудник из аптеки на которую назаначена задача
        if (assignedClient.getGroupClient().getName() != task.getAssignedGroupClient().getName()) {
            throw new ClientBelongsToAnotherGroupException(assignedClient.getUsername());
        }

        //Распределение на сотрудника
        task.setAssignedClient(assignedClient);
        //Перестаёт принадлежать группе
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

        //Распределение на сотрудника
        task.setAssignedClient(assignedClient);
        //Перестаёт принадлежать группе
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
        
        GroupClient assignedGroupClient = groupClientService.getOne(assignedGroupClientId);

        //Распределение на группу
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
