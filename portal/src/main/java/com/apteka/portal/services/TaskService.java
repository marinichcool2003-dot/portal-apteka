package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.TaskAuditService;
import com.apteka.portal.components.servicesecurity.TaskSecurityService;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.task.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.task.TaskRequestDTO;
import com.apteka.portal.dtos.request.task.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.exceptions.AccountNotFoundException;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AccountRepository;
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
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(TaskShortResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getOne(Long id, AppUserDetails currentUser) {
        Task task = taskRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskSecurityService.canSelectTask(task, currentUser);

        task = taskRepository.fetchPictures(id).orElse(task);
        task = taskRepository.fetchCommentsForTask(id).orElse(task);

        return TaskResponseDTO.from(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getDepartmentTaskWithFilters(DepartamentTaskWithFiltersDTO dto,
            Pageable pageable) {
        return fetchAndMapTasks(dto, pageable);
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
    public Page<TaskShortResponseDTO> getMyDepartmentTasks(DepartamentTaskWithFiltersDTO dto,
            AppUserDetails currentUser, Pageable pageable) {
        var dtoBuilder = dto.toBuilder();
        dtoBuilder.assignerId(currentUser.getInternalId());
        dtoBuilder.creatorId(null);

        return fetchAndMapTasks(dtoBuilder.build(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getCreatedMeTasks(DepartamentTaskWithFiltersDTO dto, AppUserDetails currentUser,
            Pageable pageable) {
        var dtoBuilder = dto.toBuilder();
        dtoBuilder.creatorId(currentUser.getInternalId());
        dtoBuilder.assignerId(null);
        return fetchAndMapTasks(dtoBuilder.build(), pageable);
    }

    private Page<TaskShortResponseDTO> fetchAndMapTasks(DepartamentTaskWithFiltersDTO dto, Pageable pageable) {
        Specification<Task> specification = TaskSpecifications.getTaskWithFilters(dto);

        Page<Task> taskPage = taskRepository.findAll(specification, pageable);

        if (taskPage.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> taskIds = taskRepository.findAll(specification).stream()
                .map(Task::getId)
                .toList();

        List<Task> heavyTasks = taskRepository.findShortTasksByIds(taskIds);

        List<TaskShortResponseDTO> dtoList = taskIds.stream()
                .map(id -> heavyTasks.stream().filter(t -> t.getId().equals(id)).findFirst().orElseThrow())
                .map(TaskShortResponseDTO::from)
                .toList();

        return new PageImpl<>(dtoList, pageable, taskPage.getTotalElements());
    }

    @Transactional
    public TaskShortResponseDTO create(TaskCreateRequestDTO dto, AppUserDetails currentUser) {

        Account account = null;
        if (dto.assignerId() != null) {
            account = accountRepository.findById(dto.assignerId())
                    .orElseThrow(() -> new AccountNotFoundException(dto.assignerId()));
        }

        WorkType workType = workTypeRepository.findById(dto.workTypeId())
                .orElseThrow(() -> new WorkTypeNotFoundException(dto.workTypeId()));

        taskSecurityService.validateCanCreateTask(account, workType, currentUser);

        String cleanTitle = typeNameValidator.getCleanName(dto.title());

        validateTitle(cleanTitle);
        validateDescription(dto.description());

        Task task = Task.builder()
                .title(cleanTitle)
                .description(dto.description().strip())
                .workType(workType)
                .build();

        switch (currentUser.getType()) {
            case APTEKA -> {
                Apteka apteka = aptekaRepository.findById(currentUser.getInternalId())
                        .orElseThrow(() -> new AptekaNotFoundException(currentUser.getInternalId()));
                task.setCreatedByApteka(apteka);
            }
            case CLIENT -> {
                Client client = clientRepository.findById(currentUser.getInternalId())
                        .orElseThrow(() -> new ClientNotFoundException(currentUser.getInternalId()));
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

        if (dto.statusCode() != null && !dto.statusCode().isBlank()
                && !Objects.equals(task.getStatus().getCode(), dto.statusCode())) {
            task = changeStatus(task, dto.statusCode(), currentUser);
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
        taskSecurityService.validateCanDelete(currentUser);

        taskRepository.delete(task);
        var event = new SseEventNames.EntityUpdateSignalDTO(task.getWorkType().getId(), SseSignalTypes.DELETED);
        sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
    }

    private Task changeStatus(Task task, String code, AppUserDetails currentUser) {
        taskSecurityService.validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromCode(code);

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
            String ident = (number != null) ? "№" + number : task.getAssignedApteka().getAccount().getLogin();
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