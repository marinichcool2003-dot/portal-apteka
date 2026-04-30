package com.apteka.portal.components;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;
import com.apteka.portal.services.ClientService;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskSecurityService {

    private final ClientService clientService;

    public void validateCanCreate(TaskRequestDTO dto, AppUserDetails currentUser) {
        if (currentUser.getType() == UserType.CLIENT) {
            if (hasElevatedPrivileges(currentUser)) {
                return;
            }

            if (currentUser.isJustUser()) {
                // Логика: обычный юзер не может назначать задачи чужим группам (кроме аптек)
                boolean isAssigningToHisGroup = Objects.equals(currentUser.getUserGroup().getId(),
                        dto.assignedGroupId());
                if (!isAssigningToHisGroup && dto.assignedAptekaId() == null) {
                    throw new AccessDeniedException(
                            "Вы можете ставить задачи только сотрудникам своей группы или аптекам");
                }
            }
        }

        if (currentUser.getType() == UserType.APTEKA) {
            if (dto.assignedGroupId() != null && dto.assignedClientId() != null) {
                throw new AccessDeniedException("Аптека не может ставить задачи на конкретного сотрудника");
            }
        }
    }

    public void validateCanUpdate(Task task, TaskRequestDTO dto, AppUserDetails currentUser) {
        boolean assignmentChanged = isAssignmentChanged(task, dto);

        if (assignmentChanged) {
            if (currentUser.getType() == UserType.CLIENT) {
                if (currentUser.isJustUser() && !isUserRelatedToTask(task, currentUser)) {
                    if (dto.assignedGroupId() != null
                            && !Objects.equals(dto.assignedGroupId(), currentUser.getUserGroup().getId())) {
                        throw new AccessDeniedException(
                                "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!");
                    }
                }
            }

            if (currentUser.getType() == UserType.APTEKA) {
                if (!isAptekaRelatedToTask(task, currentUser)) {
                    throw new AccessDeniedException("Аптека не может изменять задачу, к которой не имеет отношения!");
                }
            }
        }
    }

    public boolean changeAssigner(Task task, TaskRequestDTO dto, AppUserDetails currentUser) {
        if(isAssignmentChanged(task, dto)) {
            if (hasElevatedPrivileges(currentUser)) return true;
            if (!isUserRelatedToTask(task, currentUser) || !isAssignerOfSameGroup(dto, currentUser)) 
                throw new AccessDeniedException("Пользователь без прав доступа может изменять исполнителей только своих собственных или назначенных ему задач и переводить их внутри своей группы");
            return true;
        }
        return false;
    }

    public void validateStatus(Task task, AppUserDetails currentUser) {
        if (isTaskLockedForChanges(task)) {
            throw new BlockChangeIfNotActuallyTaskException();
        }

        if (currentUser.getType() == UserType.CLIENT) {
            if (hasElevatedPrivileges(currentUser))
                return;

            if (!isUserRelatedToTask(task, currentUser)) {
                throw new AccessDeniedException(
                        "Пользователь без прав доступа может изменять только свои собственные или назначенные ему задачи");
            }
        }

        if (currentUser.getType() == UserType.APTEKA) {
            if (!isAptekaRelatedToTask(task, currentUser)) {
                throw new AccessDeniedException(
                        "Аптека может изменять только свои собственные или назначенные ей задачи");
            }
        }
    }

    private boolean hasElevatedPrivileges(AppUserDetails user) {
        return user.getRoles().contains(UserRole.ADMIN)
                || user.getRoles().contains(UserRole.BOSS)
                || user.getRoles().contains(UserRole.SENIOR);
    }

    private boolean isUserRelatedToTask(Task task, AppUserDetails user) {
        return Objects.equals(getAssignedClientId(task), user.getClientId())
                || Objects.equals(getCreatedByClientId(task), user.getClientId());
    }

    private boolean isAptekaRelatedToTask(Task task, AppUserDetails user) {
        return Objects.equals(getCreatedByAptekaId(task), user.getAptekaId())
                || Objects.equals(getAssignedAptekaId(task), user.getAptekaId());
    }

    private boolean isTaskLockedForChanges(Task task) {
        return (task.getStatus() == TaskStatus.DENIED) ||
                (task.getStatus() == TaskStatus.CLOSED
                        && LocalDateTime.now().isAfter(task.getClosingDate().plusMonths(1)));
    }

    private boolean isAssignmentChanged(Task task, TaskRequestDTO dto) {
        return !Objects.equals(getAssignedAptekaId(task), dto.assignedAptekaId()) ||
                !Objects.equals(getAssignedClientId(task), dto.assignedClientId()) ||
                !Objects.equals(getAssignedGroupId(task), dto.assignedGroupId());
    }

    private boolean isAssignerOfSameGroup(TaskRequestDTO dto, AppUserDetails currentUser) {
        return !Objects.equals(currentUser.getUserGroup().getId(), dto.assignedGroupId()) && 
            !Objects.equals(currentUser.getUserGroup().getId(), clientService.getOne(dto.assignedClientId()).getUserGroup().getId());
    }

    // Безопасное извлечение ID из сущностей (Null-safe)
    private Integer getAssignedAptekaId(Task task) {
        return task.getAssignedApteka() != null ? task.getAssignedApteka().getId() : null;
    }

    private UUID getAssignedClientId(Task task) {
        return task.getAssignedClient() != null ? task.getAssignedClient().getId() : null;
    }

    private Integer getAssignedGroupId(Task task) {
        return task.getAssignedGroup() != null ? task.getAssignedGroup().getId() : null;
    }

    private Integer getCreatedByAptekaId(Task task) {
        return task.getCreatedByApteka() != null ? task.getCreatedByApteka().getId() : null;
    }

    private UUID getCreatedByClientId(Task task) {
        return task.getCreatedByClient() != null ? task.getCreatedByClient().getId() : null;
    }
}
