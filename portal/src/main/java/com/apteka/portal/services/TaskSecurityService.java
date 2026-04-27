package com.apteka.portal.services;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.ClientRole;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UsersInApp;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSecurityService {
    public void validateCanCreate(TaskRequestDTO dto, UsersInApp currentUser) {
        if (currentUser instanceof Client client) {
            if (client.getRole() == ClientRole.ADMIN || client.getRole() == ClientRole.BOSS
                    || client.getRole() == ClientRole.SENIOR)
                return;
            if (client.getRole() == ClientRole.USER) {
                if (Objects.equals(client.getUserGroup().getId(), dto.assignedGroupId()) && dto.assignedAptekaId() == null) {
                    throw new AccessDeniedException(
                            "Вы можете ставить задачи только сотрудникам своей группы или аптекам");
                }
            }
        }
        if (currentUser instanceof Apteka) {

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
            if (currentUser instanceof Client client) {
                if (justUser(client)
                        && !Objects.equals(getAssignedClientId(task), client.getId())
                        && !Objects.equals(getCreatedByClientId(task), client.getId())
                        && (!Objects.equals(getAssignedGroupId(task), client.getUserGroup().getId())
                                && (dto.assignedGroupId() != null
                                        && dto.assignedGroupId() != client.getUserGroup().getId()))) {
                    throw new AccessDeniedException(
                            "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!");
                }
            }
            if (currentUser instanceof Apteka apteka) {
                if (!Objects.equals(getCreatedByAptekaId(task), apteka.getId())
                        && !Objects.equals(getAssignedAptekaId(task), apteka.getId())) {
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

        if (currentUser instanceof Client client) {
            if (!justUser(client))
                return;

            if (!Objects.equals(getAssignedClientId(task), client.getId())
                    && !Objects.equals(getCreatedByClientId(task), client.getId())) {
                throw new AccessDeniedException(
                        "Пользователь без определенных прав может изменять статус только своих собственных задач");
            }
        }

        if (currentUser instanceof Apteka apteka) {
            if (!Objects.equals(getCreatedByAptekaId(task), apteka.getId())
                    && !Objects.equals(getAssignedAptekaId(task), apteka.getId())) {
                throw new AccessDeniedException("Аптека может изменять статус только своих собственных задач");
            }
        }
    }

    private Integer getAssignedAptekaId(Task task) {
        return task.getAssignedApteka() != null
                ? task.getAssignedApteka().getId()
                : null;
    }

    private UUID getAssignedClientId(Task task) {
        return task.getAssignedClient() != null
                ? task.getAssignedClient().getId()
                : null;
    }

    private Integer getAssignedGroupId(Task task) {
        return task.getAssignedGroup() != null
                ? task.getAssignedGroup().getId()
                : null;
    }

    private Integer getCreatedByAptekaId(Task task) {
        return task.getCreatedByApteka() != null
                ? task.getCreatedByApteka().getId()
                : null;
    }

    private UUID getCreatedByClientId(Task task) {
        return task.getCreatedByClient() != null
                ? task.getCreatedByClient().getId()
                : null;
    }

    private boolean justUser(Client client) {
        if (client.getRole() == ClientRole.USER) {
            return true;
        }
        return false;
    }
}
