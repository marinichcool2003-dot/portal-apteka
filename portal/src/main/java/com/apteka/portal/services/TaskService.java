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
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.repository.TaskInterface;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskService {

    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final AptekaService aptekaService;
    private final TaskInterface taskInterface;
    private final GroupTaskService groupTaskService;
    private final ClientService clientService;

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getAll() {
        log.info("Загрузка всех задач (выполняется в потоке: {})", Thread.currentThread().getName());
        return CompletableFuture.completedFuture(taskInterface.findAll());
    }

    @SuppressWarnings("null")
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<Task> getOne(Long id) {
        log.info("Поиск задачи с ID={} (в потоке: {})", id, Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> taskInterface.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id)));
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByApteka(Integer aptekaId) {
        log.info("Загрузка задач для аптеки ID={} (в потоке: {})", aptekaId, Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> {
            aptekaService.getOne(aptekaId);
            return taskInterface.findByAptekaId(aptekaId);
        });
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByClient(UUID clientId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Поиск задач для исполнителя ID={} (в потоке: {})", clientId, Thread.currentThread());
            clientService.getOne(clientId);
            return taskInterface.findByClientId(clientId);
        });
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByGroup(Integer groupId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Поиск по типу задачи ID={} (в потоке: {})", groupId, Thread.currentThread());
            groupTaskService.getOne(groupId);
            return taskInterface.findByGroupId(groupId);
        });
    }

    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> getByCreatedByClient(UUID createdByClientId) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("Поиск созданных специалистом задач ID={} (в потоке: {})", createdByClientId,
                    Thread.currentThread());
            clientService.getOne(createdByClientId);
            return taskInterface.findByCreatedByClient(createdByClientId);
        });
    }


    // Фильтр задач
    @Async
    @Transactional(readOnly = true)
    public CompletableFuture<List<Task>> filter(UUID clientId,
            Integer aptekaId,
            UUID createdByClientId,
            Integer groupId,
            TaskStatus status,
            Date fromDate,
            Date toDate) {

        return CompletableFuture.supplyAsync(() -> {
            log.info("Фильтр по задаче (в потоке: {})", Thread.currentThread());

            return taskInterface.filter(clientId,
                    aptekaId,
                    aptekaId,
                    createdByClientId,
                    groupId,
                    status,
                    fromDate,
                    toDate);
        });
    }

    // Создание задачи аптекой для сотрудника
    @SuppressWarnings("null")
    @Async
    @Transactional
    public CompletableFuture<Task> create(String title, String description, String comments, Integer aptekaId,
            Integer groupId) {
        log.info("Создание новой задачи для аптеки: ID={} (в потоке: {})", aptekaId, Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> {

            if (title == null || title.isBlank())
                throw new InvalidTaskTitleException();
            if (description == null || description.isBlank())
                throw new InvalidTaskDescriptionException();

            Apteka apteka = aptekaService.getOne(aptekaId);
            GroupTask group = groupTaskService.getOne(groupId);

            Task task = Task.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .comments(comments != null ? comments.strip() : null)
                    .date(new Date())
                    .apteka(apteka)
                    .group(group)
                    .build();

            return taskInterface.save(task);
        });
    }

    // Создание задачи сотрудником для сотрудника
    @SuppressWarnings("null")
    @Async
    @Transactional
    public CompletableFuture<Task> create(String title, String description, String comments, UUID clientId,
            Integer groupId) {
        log.info("Создание новой задачи для сотрудника: ID={} (в потоке: {})", clientId,
                Thread.currentThread().getName());
        return CompletableFuture.supplyAsync(() -> {

            if (title == null || title.isBlank())
                throw new InvalidTaskTitleException();
            if (description == null || description.isBlank())
                throw new InvalidTaskDescriptionException();

            Client client = clientService.getOne(clientId);
            GroupTask group = groupTaskService.getOne(groupId);

            Task task = Task.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .comments(comments != null ? comments.strip() : null)
                    .date(new Date())
                    .createdByClient(client)
                    .group(group)
                    .build();

            return taskInterface.save(task);
        });
    }

    // Открытие задачи
    @Async
    @Transactional
    public CompletableFuture<Task> openTask(Long id) {
        log.info("Изменение статуса задачи ID={} на статус \"OPEN\" (в потоке: {})", id,
                Thread.currentThread().getName());

        return getOne(id).thenApply(task -> {
            task.setStatus(TaskStatus.OPEN);
            return taskInterface.save(task);
        });
    }

    // Закрытие задачи
    @Async
    @Transactional
    public CompletableFuture<Task> closeTask(Long id) {
        log.info("Закрытие задачи ID={} (в потоке: {})", id, Thread.currentThread().getName());

        return getOne(id).thenApply(task -> {
            task.setStatus(TaskStatus.CLOSED);
            return taskInterface.save(task);
        });
    }

    // Задача в процессе
    @Async
    @Transactional
    public CompletableFuture<Task> processedTask(Long id) {
        log.info("Изменение статуса задачи ID={} на статус \"PROCESED\" (в потоке: {})", id,
                Thread.currentThread().getName());

        return getOne(id).thenApply(task -> {
            task.setStatus(TaskStatus.PROCESSED);
            return taskInterface.save(task);
        });
    }

    // Задача отклонена
    @Async
    @Transactional
    public CompletableFuture<Task> deniedTask(Long id) {
        log.info("Изменение статуса задачи ID={} на статус \"DENIED\" (в потоке: {})", id,
                Thread.currentThread().getName());

        return getOne(id).thenApply(task -> {
            task.setStatus(TaskStatus.DENIED);
            return taskInterface.save(task);
        });
    }

    @Async
    @Transactional
    public CompletableFuture<Task> setClient(Long id, UUID clientId) {
        log.info("Распределение задачи ID={} сотруднику ID={} (в потоке: {})", id, clientId,
                Thread.currentThread().getName());
        return getOne(id).thenApply(task -> {
            task.setClient(clientService.getOne(clientId));
            return taskInterface.save(task);
        });
    }

    @SuppressWarnings("null")
    @Async
    @Transactional
    public CompletableFuture<Task> update(Long id, String title, String description, String comments) {
        log.info("Обновление задачи с ID={} (в потоке: {})", id, Thread.currentThread().getName());

        return getOne(id).thenApply(task -> {
            if (title != null && !title.isBlank())
                task.setTitle(title.strip());
            if (description != null)
                task.setDescription(description.strip());
            if (comments != null)
                task.setComments(comments.strip());
            return taskInterface.save(task);
        });
    }

    @SuppressWarnings("null")
    @Async
    @Transactional
    public CompletableFuture<Void> delete(Long id) {
        log.info("Удаление задачи с ID={} (в потоке: {})", id, Thread.currentThread().getName());

        return CompletableFuture.runAsync(() -> {
            if (!taskInterface.existsById(id)) {
                throw new TaskNotFoundException(id);
            }
            taskInterface.deleteById(id);
        });
    }
}
