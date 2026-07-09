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

    public void validateGroupTaskUpdate(AppUserDetails currentUser, GroupTask groupTask, boolean isSafeChange) {
        Integer groupTaskId = groupTask.getId();
        boolean existsActive = taskRepository.existsByGroupTaskAndStatusActive(groupTaskId);

        if (isSafeChange) {
            boolean existsNonActive = taskRepository.existsByGroupTaskAndStatusNonActive(groupTaskId);
            if (existsActive || existsNonActive) {
                throw new AccessDeniedException("По данному типу задач имеются задачи!");
            }
        } else {
            if (existsActive) {
                throw new AccessDeniedException(
                        "По данному типу задач имеются активные задачи (необходимо либо закрыть либо отклонить)!");
            }
        }

        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (isSafeChange) {
            if (currentUser.hasAction(AccountAction.GRAND_WORK_WITH_GROUP_TASK)) {
                return;
            }
            if (currentUser.hasAction(AccountAction.BASE_WORK_WITH_GROUP_TASK)
                    && sameGroup(currentUser, groupTask.getCreatorGroup())) {
                return;
            }
            throw new AccessDeniedException("У вас нет прав на изменение типа задач данной группы!");
        } else {
            if (currentUser.hasAction(AccountAction.NON_SAFE_UPDATE)) {
                return;
            }
            throw new AccessDeniedException("У вас нет прав на небезопасное изменение типа задач!");
        }
    }

    public void validateCanPermanentDelete(GroupTask groupTask, Boolean confirm) {
        Integer groupTaskId = groupTask.getId();
        boolean existsActive = taskRepository.existsByGroupTaskAndStatusActive(groupTaskId);
        if (existsActive) {
            throw new AccessDeniedException(
                    "По данному типу задач имеются активные задачи (необходимо либо закрыть либо отклонить)!");
        }
        boolean existsNonActive = taskRepository.existsByGroupTaskAndStatusNonActive(groupTaskId);
        boolean existsWorkType = workTypeRepository.existsByGroupTaskId(groupTaskId);
        if (!confirm) {
            if (existsNonActive) {
                throw new AccessDeniedException("По данному типу задач имеются завершенные задачи (Необходимо подтверждение)");
            }
            if (existsWorkType) {
                throw new AccessDeniedException("По данному типу задач имеются виды работ! (Необходимо подтверждение)");
            }
        }
    }

    public void validateGroupVisibility(Integer firstUserGroupId, Integer secondUserGroupId) {
        boolean visibilityExists = visibilityRepository.
                existsRelationBidirectional(firstUserGroupId, secondUserGroupId);
        if (!visibilityExists) {
            throw new AccessDeniedException("Вы не можете взаимодействовать с данной группой!");
        }
    }

    private boolean sameGroup(AppUserDetails currentUser, UserGroup userGroup) {
        return Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId());
    }
}
