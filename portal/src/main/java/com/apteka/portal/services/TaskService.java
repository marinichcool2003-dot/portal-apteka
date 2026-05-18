package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apteka.portal.components.TaskAuditService;
import com.apteka.portal.components.TaskSecurityService;
import com.apteka.portal.dtos.request.DepartamentTaskWithFiltersDTO;
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.TaskResponseDTO;
import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.ClientNotFoundException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

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

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAll() {
        log.info("Получение всех задач синхронно | поток {}", Thread.currentThread().getName());
        return taskRepository.findAll().stream()
                .map(TaskResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getOne(Long id) {
        log.info("Получение полной карточки задачи id={}", id);

        Task task = taskRepository.findByIdWithDetailsAndPictures(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        task = taskRepository.fetchCommentsForTask(id).orElse(task);

        return TaskResponseDTO.from(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getDepartamentTaskWithFilters(DepartamentTaskWithFiltersDTO dto) {
        List<Long> taskIds = taskRepository.findDepartmentTaskIdsWithFilters(
                dto.groupId(), dto.creatorClientId(), dto.creatorAptekaId(),
                dto.specificClientId(), dto.specificAptekaId(), dto.status(),
                dto.priority(), dto.groupTaskId());

        if (taskIds.isEmpty()) {
            return List.of();
        }

        return taskRepository.findTasksWithDetailsByIds(taskIds)
                .stream()
                .map(TaskResponseDTO::from)
                .toList();
    }

    @Transactional
    public TaskResponseDTO create(TaskCreateRequestDTO dto, AppUserDetails currentUser) {
        taskSecurityService.validateCanCreate(dto, currentUser);
        validateTitle(dto.title());
        validateDescription(dto.description());

        Task task = Task.builder()
                .title(dto.title().strip())
                .description(dto.description().strip())
                .workType(workTypeRepository.getReferenceById(dto.workTypeId()))
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
        return TaskResponseDTO.from(saved);
    }

    @Transactional
    public TaskResponseDTO update(Long id, TaskUpdateRequestDTO dto, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskSecurityService.validateCanUpdate(task, dto, currentUser);

        if (dto.title() != null && !Objects.equals(task.getTitle(), dto.title())) {
            validateTitle(dto.title());
            taskAuditService.logChange(task, currentUser, "заголовок", task.getTitle(), dto.title());
            task.setTitle(dto.title().strip());
        }

        if (dto.description() != null && !Objects.equals(task.getDescription(), dto.description())) {
            validateDescription(dto.description());
            taskAuditService.logChange(task, currentUser, "описание", task.getDescription(), dto.description());
            task.setDescription(dto.description().strip());
        }

        if (taskSecurityService.changeWorkTypeToAnotherDepartament(task, dto, currentUser)) {
            task.setWorkType(workTypeRepository.getReferenceById(dto.workTypeId()));
        }

        if (taskSecurityService.changeAssigner(task, dto, currentUser)) {
            String oldAssigneeName = getAssigneeName(task);
            setAssignee(task, dto, currentUser);
            String newAssigneeName = getAssigneeName(task);
            taskAuditService.logChange(task, currentUser, "исполнителя", oldAssigneeName, newAssigneeName);
        }

        if (dto.statusDescription() != null && !dto.statusDescription().isBlank()) {
            task = changeStatus(task, dto.statusDescription(), currentUser);
        }

        task.setUpdatedDate(LocalDateTime.now());
        Task saved = taskRepository.save(task);

        return TaskResponseDTO.from(saved);
    }

    @Transactional
    public void delete(Long id, AppUserDetails currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));

        if (!currentUser.hasRole(UserRole.ADMIN)) {
            throw new AccessDeniedException("Только пользователь с правами администратора может удалить задачу");
        }
        taskRepository.delete(task);
    }

    private Task changeStatus(Task task, String statusDescription, AppUserDetails currentUser) {
        taskSecurityService.validateStatus(task, currentUser);
        TaskStatus newStatus = TaskStatus.fromDescription(statusDescription);

        if (newStatus == task.getStatus()) {
            return task;
        }

        TaskStatus oldStatus = task.getStatus();
        task.changeStatus(newStatus);

        String commentText = "Пользователь %s изменил статус задачи #%d c '%s' на '%s'"
                .formatted(taskAuditService.getAuthor(currentUser), task.getId(), oldStatus.name(), newStatus.name());

        taskAuditService.addComment(commentText, currentUser, task);
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