package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
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
    private final TaskCommentsService taskCommentsService;
    private final TaskSecurityService taskSecurityService;

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

        return taskRepository.save(task);

    }

    @Transactional
    public Task update(Long id, TaskRequestDTO dto, String statusDescription, UsersInApp currentUser) {

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (task.getAssignedApteka().getId() != dto.assignedAptekaId() ||
                task.getAssignedClient().getId() != dto.assignedClientId() ||
                task.getAssignedGroup().getId() != dto.assignedGroupId()) {
            if (currentUser instanceof Client client) {
                if (justUser(client) && task.getAssignedClient().getId() != client.getId()
                        && task.getCreatedByClient().getId() != client.getId()
                        && (client.getUserGroup().getId() != task.getAssignedGroup().getId()
                                && dto.assignedGroupId() != client.getUserGroup().getId())) {
                    throw new AccessDeniedException(
                            "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!");
                }
            }
            if (currentUser instanceof Apteka apteka) {
                if (task.getCreatedByApteka().getId() != apteka.getId()
                        && task.getAssignedApteka().getId() != apteka.getId()) {
                    throw new AccessDeniedException("Аптека не может изменять задачу к которой не имеет отношение!");
                }
            }
        }

        validateTitleAndDescripton(dto.title(), dto.description());
        if (statusDescription != null && !statusDescription.isBlank()) {
            task = changeStatus(task, statusDescription, currentUser);
        }

        if (dto.comments() != null && !dto.comments().isBlank()) {
            task.setComments(dto.comments().strip());
        }

        task.setUpdatedDate(LocalDateTime.now());

        return taskRepository.save(task);
    }

    @Transactional
    public void delete(Long id, UsersInApp currentUser) {

        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException(id);
        }

        if (currentUser instanceof Client client) {
            if (client.getRole() == ClientRole.ADMIN) {
                taskRepository.deleteById(id);
            }
        }
        throw new AccessDeniedException("Только пользователь с правами администратора может удалить задачу");
    }

    private Task changeStatus(Task task, String statusDescription, UsersInApp currentUser) {

        validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus != task.getStatus()) {
            TaskStatus oldStatus = task.getStatus();
            task.setStatus(newStatus);

            if (newStatus == TaskStatus.CLOSED) {
                task.setClosingDate(LocalDateTime.now());
            }
            task.setUpdatedDate(LocalDateTime.now());

            String authorName = "Система";
            if (currentUser instanceof Client client) {
                authorName = clientService.getOne(client.getId()).getFullName();
            } else if (currentUser instanceof Apteka apteka) {
                authorName = aptekaService.getOne(apteka.getId()).getUserGroup().getName() + " "
                        + aptekaService.getOne(apteka.getId()).getNumber();
            }

            String commentText = "Пользователь %s изменил статус задачи #%d изменен c '%s' на '%s' "
                    .formatted(authorName, task.getId(), oldStatus, newStatus);

            Task savedTask = taskRepository.save(task);
            taskCommentsService.create(commentText, task.getId(), currentUser);
            return savedTask;
        }

        return task;
    }

    private Integer getAssignedAptekaId(Task task) {
        return task.getAssignedApteka() != null 
            ? task.getAssignedApteka().getId()
            : null;
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

    private void validateStatus(Task task, UsersInApp currentUser) {
        if ((task.getStatus() == TaskStatus.DENIED) || (task.getStatus() == TaskStatus.CLOSED
                && LocalDateTime.now().isAfter(task.getClosingDate().plusMonths(1)))) {
            throw new BlockChangeIfNotActuallyTaskException();
        }

        if (currentUser instanceof Client client) {
            if (!justUser(client))
                return;

            if (client.getId() != task.getAssignedClient().getId()
                    && client.getId() != task.getCreatedByClient().getId()) {
                throw new AccessDeniedException(
                        "Пользователь без определенных прав может изменять статус только своих собственных задач");
            }
        }

        if (currentUser instanceof Apteka apteka) {
            if (apteka.getId() != task.getCreatedByApteka().getId()
                    && apteka.getId() != task.getAssignedApteka().getId()) {
                throw new AccessDeniedException("Аптека может изменять статус только своих собственных задач");
            }
        }
    }

    private boolean justUser(Client client) {
        if (client.getRole() != ClientRole.ADMIN && client.getRole() != ClientRole.BOSS
                && client.getRole() != ClientRole.SENIOR) {
            return true;
        }
        return false;
    }
}
