package com.apteka.portal.components.servicesecurity;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.repository.TaskRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class WorkTypeSecurityService {

    private final TaskRepository taskRepository;

    public void validateCanWorkWorkType(AppUserDetails currentUser, UserGroup userGroup) {
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

    public void validateWorkTypeUpdate(AppUserDetails currentUser,
            WorkType workType,
            boolean nameChanged,
            boolean groupsChanged,
            boolean confirm) {
        Integer workTypeId = workType.getId();

        boolean isSuperUser = currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAction(AccountAction.NON_SAFE_UPDATE_WORK_TYPE);

        if (isSuperUser) {
            if ((groupsChanged || nameChanged) && !confirm) {
                throw new AccessDeniedException("Для выполнения данной операции требуется подтверждение (confirm)!");
            }
            return;
        }

        if (groupsChanged) {
            boolean hasActive = taskRepository.existsByWorkTypeAndStatusActive(workTypeId);
            boolean hasNonActive = taskRepository.existsByGroupTaskAndStatusNonActive(workTypeId);

            if (hasNonActive || hasActive) {
                throw new AccessDeniedException(
                        "Невозможно изменить вид работ: по данному виду работ уже есть связанные заявки!");
            }
        }

        if (nameChanged) {
            if (taskRepository.existsByWorkTypeAndStatusActive(workTypeId)) {
                throw new AccessDeniedException(
                        "Невозможно изменить имя: по данному типу задач имеются активные заявки!");
            }
        }

        if (currentUser.hasAction(AccountAction.GRAND_WORK_WITH_WORK_TYPE)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.BASE_WORK_WITH_WORK_TYPE)
                && sameGroup(currentUser, workType.getGroupTask().getCreatorGroup())) {
            return;
        }

        throw new AccessDeniedException("У вас нет прав на изменение типа задач данной группы!");
    }

    public void validateCanPermanentDelete(WorkType workType, Boolean confirm) {
        Integer workTypeId = workType.getId();
        boolean existsActive = taskRepository.existsByWorkTypeAndStatusActive(workTypeId);
        if (existsActive) {
            throw new AccessDeniedException(
                    "Невозможно изменить имя: по данному типу задач имеются активные заявки!");
        }
        boolean existsNonActive = taskRepository.existsByWorkTypeAndStatusNonActive(workTypeId);
        if (!confirm) {
            if (existsNonActive) {
                throw new AccessDeniedException(
                        "По данному виду работ имеются завершенные задачи (Необходимо подтверждение)");
            }
        }
    }

    public boolean isGroupTaskCreatorGroup(AppUserDetails currentUser, GroupTask groupTask) {
        return Objects.equals(currentUser.getUserGroup().getId(), groupTask.getCreatorGroup().getId());
    }

    private boolean sameGroup(AppUserDetails currentUser, UserGroup userGroup) {
        return Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId());
    }
}
