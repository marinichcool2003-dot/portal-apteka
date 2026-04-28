package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UsersInApp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSecurityService {
    public void validateCanCreate(TaskRequestDTO dto, UsersInApp currentUser) {
        if (currentUser.isClient()) {
            if (currentUser.hasRole(UserRole.ADMIN) 
                    || currentUser.hasRole(UserRole.BOSS) 
                    || currentUser.hasRole(UserRole.SENIOR))
                return;
            if (currentUser.isJustUser()) {
                if (Objects.equals(currentUser.getUserGroup().getId(), dto.assignedGroupId()) 
                        && dto.assignedAptekaId() == null) {
                    throw new AccessDeniedException(
                            "Вы можете ставить задачи только сотрудникам своей группы или аптекам");
                }
            }
        }
        if (currentUser.isApteka()) {

            if (dto.assignedGroupId() != null && dto.assignedClientId() != null) {
                throw new AccessDeniedException("Аптека не может ставить задачи на конкретного сотрудника");
            }
        }
    }

    public void validateCanUpdate(Task task, TaskRequestDTO dto, UsersInApp currentUser) {
        boolean assignmentChanged = !Objects.equals(getAssignedAptekaId(task), dto.assignedAptekaId()) ||
                !Objects.equals(getAssignedClientId(task), dto.assignedClientId()) ||
                !Objects.equals(getAssignedGroupId(task), dto.assignedGroupId());
                
        if (assignmentChanged) {
            if (currentUser.isClient()) {
                if (currentUser.isJustUser()
                        && !Objects.equals(getAssignedClientId(task), currentUser.getClientId())
                        && !Objects.equals(getCreatedByClientId(task), currentUser.getClientId())
                        && (!Objects.equals(getAssignedGroupId(task), currentUser.getUserGroup().getId())
                                && (dto.assignedGroupId() != null
                                        && dto.assignedGroupId() != currentUser.getUserGroup().getId()))) {
                    throw new AccessDeniedException(
                            "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!");
                }
            }
            if (currentUser.isApteka()) {
                if (!Objects.equals(getCreatedByAptekaId(task), currentUser.getAptekaId())
                        && !Objects.equals(getAssignedAptekaId(task), currentUser.getAptekaId())) {
                    throw new AccessDeniedException("Аптека не может изменять задачу к которой не имеет отношение!");
                }
            }
        }
    }

    public void validateStatus(Task task, UsersInApp currentUser) {
        if ((task.getStatus() == TaskStatus.DENIED) || (task.getStatus() == TaskStatus.CLOSED
                && LocalDateTime.now().isAfter(task.getClosingDate().plusMonths(1)))) {
            throw new BlockChangeIfNotActuallyTaskException();
        }

        if (currentUser.isClient()) {
            if (!currentUser.isJustUser())
                return;

            if (!Objects.equals(getAssignedClientId(task), currentUser.getClientId())
                    && !Objects.equals(getCreatedByClientId(task), currentUser.getClientId())) {
                throw new AccessDeniedException(
                        "Пользователь без определенных прав может изменять статус только своих собственных задач");
            }
        }

        if (currentUser.isApteka()) {
            if (!Objects.equals(getCreatedByAptekaId(task), currentUser.getAptekaId())
                    && !Objects.equals(getAssignedAptekaId(task), currentUser.getAptekaId())) {
                throw new AccessDeniedException("Аптека может изменять статус только своих собственных задач");
            }
        }
    }

    private Integer getAssignedAptekaId(Task task) {
        return task.getAssignedApteka() != null
                ? task.getAssignedApteka().getAptekaId()
                : null;
    }

    private UUID getAssignedClientId(Task task) {
        return task.getAssignedClient() != null
                ? task.getAssignedClient().getClientId()
                : null;
    }

    private Integer getAssignedGroupId(Task task) {
        return task.getAssignedGroup() != null
                ? task.getAssignedGroup().getId()
                : null;
    }

    private Integer getCreatedByAptekaId(Task task) {
        return task.getCreatedByApteka() != null
                ? task.getCreatedByApteka().getAptekaId()
                : null;
    }

    private UUID getCreatedByClientId(Task task) {
        return task.getCreatedByClient() != null
                ? task.getCreatedByClient().getClientId()
                : null;
    }

}
