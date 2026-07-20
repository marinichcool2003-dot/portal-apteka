package com.apteka.portal.services;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

import com.apteka.portal.components.TaskAuditService;
import com.apteka.portal.components.servicesecurity.TaskSecurityService;
import com.apteka.portal.components.validators.SortingValidator;
import com.apteka.portal.components.validators.TypeNameValidator;
import com.apteka.portal.controllers.SseController;
import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.task.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.task.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.exceptions.AccountNotFoundException;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.exceptions.WorkTypeNotFoundException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.CacheNames;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.repository.AccountRepository;
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
    private final TaskSecurityService taskSecurityService;
    private final TaskAuditService taskAuditService;
    private final TypeNameValidator typeNameValidator;
    private final SseController sseController;
    private final AccountRepository accountRepository;
    private final SortingValidator sortingValidator;

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "creationDate",
            "updatedDate",
            "closingDate",
            "status",
            "workType.name",
            "workType.priority",
            "workType.groupTask.name",
            "workType.groupTask.creatorGroup.id",
            "workType.groupTask.intendedGroup.id",
            "creator.userGroup.id",
            "assigner.userGroup.id");

    private static final Sort DEFAULT_SORT = Sort.by(
            Sort.Order.asc("creationDate"));

    

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getAll(Pageable pageable) {

        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);

        return taskRepository.findAll(validatedPageable)
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
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        return fetchAndMapTasks(dto, validatedPageable);
    }

    @Cacheable(value = CacheNames.GROUPS_USER_STATS, sync = true)
    @Transactional(readOnly = true)
    public List<DepartmentTaskStatsDTO> getGroupsUserStats() {
        return taskRepository.findGroupUserStats();
    }

    @Cacheable(value = CacheNames.GROUPS_USER_STATS, key = "#userGroupId", sync = true)
    @Transactional(readOnly = true)
    public DepartmentTaskStatsDTO getGroupUserStats(Integer userGroupId) {
        return taskRepository.findGroupUserStatsByGroup(userGroupId)
                .orElseThrow(() -> new GroupUserNotFoundException(userGroupId));
    }

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getMyDepartmentTasks(DepartamentTaskWithFiltersDTO dto,
            AppUserDetails currentUser, Pageable pageable) {
                
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        var dtoBuilder = dto.toBuilder();
        dtoBuilder.assignerId(currentUser.getInternalId());
        dtoBuilder.creatorId(null);

        return fetchAndMapTasks(dtoBuilder.build(), validatedPageable);
    }

    @Transactional(readOnly = true)
    public Page<TaskShortResponseDTO> getCreatedMeTasks(DepartamentTaskWithFiltersDTO dto, AppUserDetails currentUser,
            Pageable pageable) {
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        var dtoBuilder = dto.toBuilder();
        dtoBuilder.creatorId(currentUser.getInternalId());
        dtoBuilder.assignerId(null);
        return fetchAndMapTasks(dtoBuilder.build(), validatedPageable);
    }

    private Page<TaskShortResponseDTO> fetchAndMapTasks(DepartamentTaskWithFiltersDTO dto, Pageable pageable) {
        Pageable validatedPageable = sortingValidator.validateAndFixSorting(pageable, ALLOWED_SORT_FIELDS, DEFAULT_SORT);
        Specification<Task> specification = TaskSpecifications.getTaskWithFilters(dto);

        Page<Task> taskPage = taskRepository.findAll(specification, validatedPageable);

        if (taskPage.isEmpty()) {
            return Page.empty(validatedPageable);
        }
        return taskPage.map(TaskShortResponseDTO::from);
    }

    @Transactional
    public TaskShortResponseDTO create(TaskCreateRequestDTO dto, AppUserDetails currentUser) {
        Account assigner = null;
        if (dto.assignerId() != null) {
            assigner = accountRepository.findById(dto.assignerId())
                    .orElseThrow(() -> new AccountNotFoundException(dto.assignerId()));
        }

        WorkType workType = workTypeRepository.findById(dto.workTypeId())
                .orElseThrow(() -> new WorkTypeNotFoundException(dto.workTypeId()));

        taskSecurityService.validateCanCreateTask(assigner, workType, currentUser);

        if (!StringUtils.hasText(dto.title())) {
            throw new InvalidTaskTitleException();
        }
        String cleanTitle = typeNameValidator.getCleanName(dto.title());

        if (!StringUtils.hasText(dto.description())) {
            throw new InvalidTaskDescriptionException();
        }

        Task task = Task.builder()
                .title(cleanTitle)
                .description(dto.description().strip())
                .workType(workType)
                .build();

        Account creator = accountRepository.findById(currentUser.getInternalId())
                .orElseThrow(() -> new AccountNotFoundException(currentUser.getInternalId()));

        task.setCreator(creator);
        task.setAssigner(assigner);

        Task saved = taskRepository.save(task);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var event = new SseEventNames.TaskSignalsDTO(saved.getWorkType().getId(), SseSignalTypes.CREATED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
                }
            });
        }
        return TaskShortResponseDTO.from(saved);
    }

    @Transactional
    public TaskShortResponseDTO update(Long id, TaskUpdateRequestDTO dto, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        boolean hasChange = false;

        if (StringUtils.hasText(dto.title())) {
            String cleanTitle = typeNameValidator.getCleanName(dto.title());
            taskSecurityService.validateCanChangeTitle(task, currentUser, cleanTitle);
            if (!Objects.equals(task.getTitle(), cleanTitle)) {
                taskAuditService.logChange(task.getId(), currentUser, "заголовок", task.getTitle(), cleanTitle);
                task.setTitle(cleanTitle);
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.description()) && !Objects.equals(task.getDescription(), dto.description())) {
            taskAuditService.logChange(task.getId(), currentUser, "описание", task.getDescription(), dto.description());
            task.setDescription(dto.description().strip());
            hasChange = true;
        }

        if (dto.workTypeId() != null || dto.assignerId() != null) {
            WorkType workType = null;
            Account assigner = null;

            if (dto.workTypeId() != null) {
                workType = workTypeRepository.findById(dto.workTypeId())
                        .orElseThrow(() -> new WorkTypeNotFoundException(dto.workTypeId()));
            }
            if (dto.assignerId() != null) {
                assigner = accountRepository.findById(dto.assignerId())
                        .orElseThrow(() -> new AccountNotFoundException(dto.assignerId()));
            }

            boolean workTypeChanged = false;
            boolean assignerChanged = false;

            if (dto.workTypeId() != null) {
                Integer currentWorkTypeId = task.getWorkType() != null ? task.getWorkType().getId() : null;
                workTypeChanged = !Objects.equals(currentWorkTypeId, workType.getId());
            } else {
                workTypeChanged = task.getWorkType() != null;
            }

            if (dto.assignerId() != null) {
                UUID currentAssignerId = task.getAssigner() != null ? task.getAssigner().getId() : null;
                assignerChanged = !Objects.equals(currentAssignerId, assigner.getId());
            } else {
                assignerChanged = task.getAssigner() != null;
            }

            if (workTypeChanged || assignerChanged) {
                taskSecurityService.canChangeAssigner(task, workType, assigner, currentUser);

                if (workTypeChanged) {
                    task.setWorkType(workType);
                }
                if (assignerChanged) {
                    task.setAssigner(assigner);
                }
                hasChange = true;
            }
        }

        if (StringUtils.hasText(dto.statusCode())) {
            TaskStatus newStatus = TaskStatus.fromCode(dto.statusCode());
            taskSecurityService.validateChangeStatusInTask(task, currentUser, newStatus);
            task.changeStatus(newStatus);
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
        taskSecurityService.validateCanPermanentDeleteTask(currentUser);
        taskRepository.delete(task);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    var event = new SseEventNames.EntityUpdateSignalDTO(task.getWorkType().getId(),
                            SseSignalTypes.DELETED);
                    sseController.broadcastNotification(SseEventNames.REFRESH_TASKS, event);
                }
            });
        }
    }
}