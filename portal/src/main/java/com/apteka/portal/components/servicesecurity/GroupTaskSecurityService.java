package com.apteka.portal.components.servicesecurity;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.GroupGroupVisibilityRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class GroupTaskSecurityService {
    private final TaskRepository taskRepository;
    private final WorkTypeRepository workTypeRepository;
    private final GroupGroupVisibilityRepository visibilityRepository;

    public void validateCanWorkGroupTask(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.GRAND_WORK_WITH_GROUP_TASK)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.BASE_WORK_WITH_GROUP_TASK)
                && sameGroup(currentUser, userGroup)) {
            return;
        }
        throw new AccessDeniedException("У вас нет доступа работать с типами задач в данной группе!");
    }

    public void validateGroupTaskUpdate(AppUserDetails currentUser,
            GroupTask groupTask,
            boolean nameChanged,
            boolean groupsChanged,
            boolean confirm) {

        Integer groupTaskId = groupTask.getId();

        boolean isSuperUser = currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAction(AccountAction.NON_SAFE_UPDATE);

        if (isSuperUser) {

            if ((groupsChanged || nameChanged) && !confirm) {
                throw new AccessDeniedException("Для выполнения данной операции требуется подтверждение (confirm)!");
            }
            return;
        }

        if (groupsChanged) {
            boolean hasActive = taskRepository.existsByGroupTaskAndStatusActive(groupTaskId);
            boolean hasNonActive = taskRepository.existsByGroupTaskAndStatusNonActive(groupTaskId);
            boolean hasWorkTypes = workTypeRepository.existsByGroupTaskIdActive(groupTaskId);

            if (hasActive || hasNonActive || hasWorkTypes) {
                throw new AccessDeniedException(
                        "Невозможно изменить группы: по данному типу задач уже есть связанные заявки или виды работ!");
            }
        }

        if (nameChanged) {
            if (taskRepository.existsByGroupTaskAndStatusActive(groupTaskId)) {
                throw new AccessDeniedException(
                        "Невозможно изменить имя: по данному типу задач имеются активные заявки!");
            }
        }

        if (currentUser.hasAction(AccountAction.GRAND_WORK_WITH_GROUP_TASK)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.BASE_WORK_WITH_GROUP_TASK)
                && sameGroup(currentUser, groupTask.getCreatorGroup())) {
            return;
        }

        throw new AccessDeniedException("У вас нет прав на изменение типа задач данной группы!");
    }

    public void validateCanPermanentDelete(GroupTask groupTask, Boolean confirm) {
        Integer groupTaskId = groupTask.getId();
        boolean existsActive = taskRepository.existsByGroupTaskAndStatusActive(groupTaskId);
        if (existsActive) {
            throw new AccessDeniedException(
                    "По данному типу задач имеются активные задачи (необходимо либо закрыть либо отклонить)!");
        }
        boolean existsNonActive = taskRepository.existsByGroupTaskAndStatusNonActive(groupTaskId);
        boolean existsWorkType = workTypeRepository.existsByGroupTaskIdActive(groupTaskId);
        if (!confirm) {
            if (existsNonActive) {
                throw new AccessDeniedException(
                        "По данному типу задач имеются завершенные задачи (Необходимо подтверждение)");
            }
            if (existsWorkType) {
                throw new AccessDeniedException("По данному типу задач имеются виды работ! (Необходимо подтверждение)");
            }
        }
    }

    public void validateGroupVisibility(Integer firstUserGroupId, Integer secondUserGroupId) {
        boolean visibilityExists = visibilityRepository.existsRelationBidirectional(firstUserGroupId,
                secondUserGroupId);
        if (!visibilityExists) {
            throw new AccessDeniedException("Вы не можете взаимодействовать с данной группой!");
        }
    }

    private boolean sameGroup(AppUserDetails currentUser, UserGroup userGroup) {
        return Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId());
    }
}
