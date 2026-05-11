package com.apteka.portal.components;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.services.ClientService;
import com.apteka.portal.services.WorkTypeService;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskSecurityService {

    private final ClientService clientService;
    private final WorkTypeService workTypeService;

    public void validateCanCreate(TaskRequestDTO dto, AppUserDetails currentUser) {

        UserGroup taskGroup = getUserGroupFromWorkTypeId(dto.workTypeId());
        Integer taskGroupId = (taskGroup != null) ? taskGroup.getId() : null;

        if (currentUser.getType() == UserType.CLIENT) {

            if (dto.assignedClientId() != null) {
                Client targetClient = clientService.getOne(dto.assignedClientId());
                if (!Objects.equals(targetClient.getUserGroup().getId(), taskGroupId)) {
                    throw new AccessDeniedException("Вы можете ставить задачи сотруднику в рамке вида работ его группы");
                }
            }

            if (hasElevatedPrivileges(currentUser)) {
                return;
            }

            if (currentUser.isJustUser()) {
                Integer userGroupId = (currentUser.getUserGroup() != null) ? currentUser.getUserGroup().getId() : null;
                boolean isAssigningToHisGroup = Objects.equals(userGroupId, taskGroupId);
                if (!isAssigningToHisGroup && dto.assignedAptekaId() == null) {
                    throw new AccessDeniedException(
                            "Вы можете ставить задачи только сотрудникам своей группы или аптекам");
                }
            }
        }

        if (currentUser.getType() == UserType.APTEKA) {
            if (dto.assignedClientId() != null) {
                throw new AccessDeniedException("Аптека не может ставить задачи на конкретного сотрудника");
            }
        }
    }

    public void validateCanUpdate(Task task, TaskRequestDTO dto, AppUserDetails currentUser) {

        if (hasUpdateDescriptionTask(dto)) {
            if (currentUser.getType() == UserType.CLIENT) {
                if (currentUser.isJustUser() && !isUserRelatedToTask(task, currentUser)) {
                    throw new AccessDeniedException(
                            "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!");
                }
            }
            if (currentUser.getType() == UserType.APTEKA) {
                throw new AccessDeniedException("Аптека не может изменять описание задачи, которую уже создала");
            }
        }
    }

    public boolean changeAssigner(Task task, TaskRequestDTO dto, AppUserDetails currentUser) {
        UserGroup targetTaskGroup = getUserGroupFromWorkTypeId(dto.workTypeId());

        if (!isAssignmentChanged(task, dto, targetTaskGroup)) {
            return false;
        }

        if (hasElevatedPrivileges(currentUser)) {
            return true;
        }

        boolean isSameGroup = isWithinSameGroup(dto, currentUser, targetTaskGroup);
        boolean isRelated = isUserRelatedToTask(task, currentUser);

        if (!isSameGroup || !isRelated) {
            throw new AccessDeniedException(
                    "Пользователь без прав доступа может изменять исполнителей только своих собственных или назначенных ему задач и переводить их внутри своей группы");
        }

        return true;
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

    private boolean hasUpdateDescriptionTask(TaskRequestDTO dto) {
        return dto.title() != null || dto.description() != null ||
                dto.comments() != null || dto.workTypeId() != null;
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

    private boolean isAssignmentChanged(Task task, TaskRequestDTO dto, UserGroup targetTaskGroup) {
        Integer targetGroupId = (targetTaskGroup != null) ? targetTaskGroup.getId() : null;

        return !Objects.equals(getAssignedAptekaId(task), dto.assignedAptekaId()) ||
                !Objects.equals(getAssignedClientId(task), dto.assignedClientId()) ||
                !Objects.equals(getAssignedGroupId(task), targetGroupId);
    }

    private boolean isWithinSameGroup(TaskRequestDTO dto, AppUserDetails currentUser, UserGroup targetTaskGroup) {
        Integer userGroupId = (currentUser.getUserGroup() != null) ? currentUser.getUserGroup().getId() : null;
        Integer targetGroupId = (targetTaskGroup != null) ? targetTaskGroup.getId() : null;

        if (!Objects.equals(userGroupId, targetGroupId)) {
            return false;
        }

        if (dto.assignedClientId() != null) {
            Client newClient = clientService.getOne(dto.assignedClientId());
            Integer newClientGroupId = (newClient != null && newClient.getUserGroup() != null)
                    ? newClient.getUserGroup().getId()
                    : null;

            return Objects.equals(userGroupId, newClientGroupId);
        }

        return true;
    }

    private Integer getAssignedAptekaId(Task task) {
        return task.getAssignedApteka() != null ? task.getAssignedApteka().getId() : null;
    }

    private UUID getAssignedClientId(Task task) {
        return task.getAssignedClient() != null ? task.getAssignedClient().getId() : null;
    }

    private UserGroup getUserGroupFromWorkTypeId(Integer workTypeId) {
        WorkType workType = workTypeService.getOne(workTypeId);
        return Optional.ofNullable(workType)
                .map(WorkType::getGroupTask)
                .map(GroupTask::getUserGroup)
                .orElse(null);
    }

    private Integer getAssignedGroupId(Task task) {
        return Optional.ofNullable(task.getWorkType())
                .map(WorkType::getGroupTask)
                .map(GroupTask::getUserGroup)
                .map(UserGroup::getId)
                .orElse(null);
    }

    private Integer getCreatedByAptekaId(Task task) {
        return task.getCreatedByApteka() != null ? task.getCreatedByApteka().getId() : null;
    }

    private UUID getCreatedByClientId(Task task) {
        return task.getCreatedByClient() != null ? task.getCreatedByClient().getId() : null;
    }
}
