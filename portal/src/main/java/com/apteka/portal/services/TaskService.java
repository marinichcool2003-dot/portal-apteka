package com.apteka.portal.services;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.TaskAuditService;
import com.apteka.portal.components.servicesecurity.TaskSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskPriority;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;
import com.apteka.portal.repository.specification.TaskSpecifications;
import com.apteka.portal.models.SseEventNames;
import com.apteka.portal.models.SseSignalTypes;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final WorkTypeRepository workTypeRepository;
    private final AptekaRepository aptekaRepository;
    private final ClientRepository clientRepository;
    private final TaskSecurityService taskSecurityService;
    private final TaskAuditService taskAuditService;
    private final TypeNameValidator typeNameValidator;
    private final SseController sseController;

    @Transactional(readOnly = true)
    public List<TaskShortResponseDTO> getAll() {
        return taskRepository.findAll().stream()
                .map(TaskShortResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getOne(Long id) {
        Task task = taskRepository.findByIdWithDetailsAndPictures(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task = taskRepository.fetchCommentsForTask(id).orElse(task);

        return TaskResponseDTO.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskShortResponseDTO> getDepartmentTaskWithFilters(DepartamentTaskWithFiltersDTO dto) {
        return fetchAndMapTasks(dto);
    }

    @Cacheable(value = CacheNames.GROUPS_USER_STATS, sync = true)
    @Transactional(readOnly = true)
    public List<DepartmentTaskStatsDTO> getGroupsUserStats() {
        return taskRepository.findGroupUserStats();
    }

    @Cacheable(value = CacheNames.GROUPS_USER_STATS, key = "#userGroupId", sync = true)
    @Transactional(readOnly = true)
    public DepartmentTaskStatsDTO getGroupUserStats(Integer userGroupId) {
        return taskRepository.findGroupUserStatsByGroup(userGroupId);
    }

    @Transactional(readOnly = true)
    public List<TaskShortResponseDTO> getMyDepartmentTasks(DepartamentTaskWithFiltersDTO dto,
            AppUserDetails currentUser) {
        var dtoBuilder = dto.toBuilder();

        if (currentUser.isClient()) {
            dtoBuilder.specificClientId(currentUser.getClientId());
            dtoBuilder.specificAptekaId(null);
        } else if (currentUser.isApteka()) {
            dtoBuilder.specificAptekaId(currentUser.getAptekaId());
            dtoBuilder.specificClientId(null);
        }

        dtoBuilder.creatorAptekaId(null);
        dtoBuilder.creatorClientId(null);

        return fetchAndMapTasks(dtoBuilder.build());
    }

    @Transactional(readOnly = true)
    public List<TaskShortResponseDTO> getCreatedMeTasks(DepartamentTaskWithFiltersDTO dto, AppUserDetails currentUser) {
        var dtoBuilder = dto.toBuilder();

        if (currentUser.isClient()) {
            dtoBuilder.creatorClientId(currentUser.getClientId());
            dtoBuilder.creatorAptekaId(null);
        } else if (currentUser.isApteka()) {
            dtoBuilder.creatorAptekaId(currentUser.getAptekaId());
            dtoBuilder.creatorClientId(null);
        }

        dtoBuilder.specificAptekaId(null);
        dtoBuilder.specificClientId(null);

        return fetchAndMapTasks(dtoBuilder.build());
    }

    private List<TaskShortResponseDTO> fetchAndMapTasks(DepartamentTaskWithFiltersDTO dto) {
        Specification<Task> specification = TaskSpecifications.getTaskWithFilters(dto);

        List<Long> taskIds = taskRepository.findAll(specification).stream()
                .map(Task::getId)
                .toList();

        if (taskIds.isEmpty()) {
            return Collections.emptyList();
        }

        System.out.println(dto);

        return taskRepository.findShortTasksByIds(taskIds).stream()
                .map(TaskShortResponseDTO::from)
                .toList();
    }

    @Transactional
    public TaskShortResponseDTO create(TaskCreateRequestDTO dto, AppUserDetails currentUser) {
        taskSecurityService.validateCanCreate(dto, currentUser);

        String cleanTitle = typeNameValidator.getCleanName(dto.title());

        validateTitle(cleanTitle);
        validateDescription(dto.description());

        WorkType workType = workTypeRepository.findById(dto.workTypeId())
                .orElseThrow(() -> new WorkTypeNotFoundException(dto.workTypeId()));

        Task task = Task.builder()
                .title(cleanTitle)
                .description(dto.description().strip())
                .workType(workType)
                .build();

        switch (currentUser.getType()) {
            case APTEKA -> {
                Apteka apteka = aptekaRepository.findById(currentUser.getAptekaId())
                        .orElseThrow(() -> new AptekaNotFoundException(currentUser.getAptekaId()));
                task.setCreatedByApteka(apteka);
            }
            case CLIENT -> {
                Client client = clientRepository.findById(currentUser.getClientId())
                        .orElseThrow(() -> new ClientNotFoundException(currentUser.getClientId()));
                task.setCreatedByClient(client);
            }
        }

        setAssignee(task, dto, currentUser);

        Task saved = taskRepository.save(task);

        var event = new SseEventNames.TaskSignalsDTO(saved.getWorkType().getId(), SseSignalTypes.CREATED);
        sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
        return TaskShortResponseDTO.from(saved);
    }

    @Transactional
    public TaskShortResponseDTO update(Long id, TaskUpdateRequestDTO dto, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskSecurityService.validateCanUpdate(task, dto, currentUser);

        boolean hasChange = false;

        if (dto.title() != null) {
            String cleanTitle = typeNameValidator.getCleanName(dto.title());
            if (!Objects.equals(task.getTitle(), cleanTitle)) {
                validateTitle(cleanTitle);
                taskAuditService.logChange(task.getId(), currentUser, "заголовок", task.getTitle(), cleanTitle);
                task.setTitle(cleanTitle);
                hasChange = true;
            }
        }

        if (dto.description() != null && !Objects.equals(task.getDescription(), dto.description())) {
            validateDescription(dto.description());
            taskAuditService.logChange(task.getId(), currentUser, "описание", task.getDescription(), dto.description());
            task.setDescription(dto.description().strip());
            hasChange = true;
        }

        if (dto.workTypeId() != null && dto.workTypeId() > 0) {
            WorkType workType = workTypeRepository.findById(dto.workTypeId())
                    .orElseThrow(() -> new WorkTypeNotFoundException(dto.workTypeId()));
            if (taskSecurityService.changeWorkTypeToAnotherDepartament(task, dto, currentUser)
                    && !Objects.equals(task.getWorkType().getId(), dto.workTypeId())) {
                task.setWorkType(workType);
                hasChange = true;
            }
        }

        if (taskSecurityService.changeAssigner(task, dto, currentUser)) {
            String oldAssigneeName = getAssigneeName(task);
            setAssignee(task, dto, currentUser);
            String newAssigneeName = getAssigneeName(task);
            taskAuditService.logChange(task.getId(), currentUser, "исполнителя", oldAssigneeName, newAssigneeName);
            hasChange = true;
        }

        if (dto.statusDescription() != null && !dto.statusDescription().isBlank()
                && !Objects.equals(task.getStatus().getDescription(), dto.statusDescription())) {
            task = changeStatus(task, dto.statusDescription(), currentUser);
            hasChange = true;
        }

        if (dto.priorityDescription() != null && !dto.priorityDescription().isBlank()
                && !Objects.equals(task.getPriority().getDescription(), dto.priorityDescription())) {
            String oldPriority = task.getPriority().getDescription();
            task.setPriority(TaskPriority.fromDescription(dto.priorityDescription()));
            taskAuditService.logChange(id, currentUser, "приоритет", oldPriority, dto.priorityDescription());
            hasChange = true;
        }

        if (hasChange) {
            var event = new SseEventNames.EntityUpdateSignalDTO(task.getId(), SseSignalTypes.UPDATED);
            sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
        }

        return TaskShortResponseDTO.from(task);
    }

    @Transactional
    public void delete(Long id, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только пользователь с правами администратора может удалить задачу");
        }
        taskRepository.delete(task);
        var event = new SseEventNames.EntityUpdateSignalDTO(task.getWorkType().getId(), SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
    }

    private Task changeStatus(Task task, String statusDescription, AppUserDetails currentUser) {
        taskSecurityService.validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus == task.getStatus()) {
            return task;
        }

        String oldStatus = task.getStatus().getDescription();
        task.changeStatus(newStatus);

        String commentText = "Пользователь %s изменил статус задачи #%d c '%s' на '%s'"
                .formatted(taskAuditService.getAuthor(currentUser), task.getId(), oldStatus,
                        newStatus.getDescription());

        taskAuditService.addComment(commentText, currentUser, task.getId());
        return task;
    }

    private void setAssignee(Task task, TaskRequestDTO dto, AppUserDetails currentUser) {
        if (dto.assignedClientId() != null) {
            if (currentUser.isApteka()) {
                throw new AccessDeniedException("Аптека не может назначать задачи на конкретного сотрудника");
            }
            if (!clientRepository.existsById(dto.assignedClientId())) {
                throw new ClientNotFoundException(dto.assignedClientId());
            }
            task.setAssignedClient(clientRepository.getReferenceById(dto.assignedClientId()));
            task.setAssignedApteka(null);

        } else if (dto.assignedAptekaId() != null) {
            if (currentUser.isApteka()) {
                throw new AccessDeniedException("Аптека не может назначать задачи на другие аптеки");
            }
            if (!aptekaRepository.existsById(dto.assignedAptekaId())) {
                throw new AptekaNotFoundException(dto.assignedAptekaId());
            }
            task.setAssignedApteka(aptekaRepository.getReferenceById(dto.assignedAptekaId()));
            task.setAssignedClient(null);
        }
    }

    private String getAssigneeName(Task task) {
        StringBuilder assigneeNameBuilder = new StringBuilder();

        Optional.ofNullable(task.getWorkType())
                .map(WorkType::getGroupTask)
                .map(GroupTask::getUserGroup)
                .map(UserGroup::getName)
                .ifPresentOrElse(
                        assigneeNameBuilder::append,
                        () -> assigneeNameBuilder.append("Общая группа"));

        if (task.getAssignedClient() != null) {
            assigneeNameBuilder.append(" - ").append(task.getAssignedClient().getFullName());
            return assigneeNameBuilder.toString();
        }

        if (task.getAssignedApteka() != null) {
            Integer number = task.getAssignedApteka().getNumber();
            String ident = (number != null) ? "№" + number : task.getAssignedApteka().getLogin();
            assigneeNameBuilder.append(" - ").append("Аптека ").append(ident);
            return assigneeNameBuilder.toString();
        }

        return assigneeNameBuilder.append(" (Не назначен)").toString();
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new InvalidTaskTitleException();
        }
    }

    private void validateDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidTaskDescriptionException();
        }
    }
}