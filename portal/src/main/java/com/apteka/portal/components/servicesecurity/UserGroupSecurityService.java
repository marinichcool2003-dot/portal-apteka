package com.apteka.portal.components.servicesecurity;

import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.dtos.response.DepartmentTaskStatsDTO;
import com.apteka.portal.exceptions.GroupUserNotFoundException;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.repository.AccountRepository;
import com.apteka.portal.repository.TaskRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserGroupSecurityService {
    private final IsActiveValidator isActiveValidator;
    private final TaskRepository taskRepository;
    private final AccountRepository accountRepository;

    public void canSelectNonActive(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_NON_ACTIVE_GROUPS)) {
            return;
        }
        throw new AccessDeniedException("Вы не можете просматривать удалённые группы");
    }

    public void validateCanUpdateUserGroup(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_UPDATE_USER_GROUP)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_UPDATE_SELF_USER_GROUP) &&
                Objects.equals(currentUser.getUserGroup().getId(), userGroup.getId())) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на изменение данной группы!");
    }

    public void validateCanSafeDelete(AppUserDetails currentUser, UserGroup userGroup) {
        if (!isActiveValidator.isUserGroupActive(userGroup)) {
            throw new AccessDeniedException("Группа уже удалена");
        }

        DepartmentTaskStatsDTO stats = taskRepository.findGroupUserStatsByGroup(userGroup.getId())
                .orElseThrow(() -> new GroupUserNotFoundException(userGroup.getId()));
        if (stats.openTasks() > 0) {
            throw new AccessDeniedException("У группы ещё имеются активные задачи, удаление запрещено");
        }

        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.SAFE_DELETE_USER_GROUP)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на удаление данной группы!");
    }

    public void validateCanRestoreAfterSafeDelete(AppUserDetails currentUser, UserGroup userGroup) {
        if (isActiveValidator.isUserGroupActive(userGroup)) {
            throw new AccessDeniedException("Группа не удалена!");
        }
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_ACTIVATE_USER_GROUP_AFTER_SAFE_DELETE)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на восстановление удалённой группы!");
    }

    public void validateCanPermanentDelete(AppUserDetails currentUser, UserGroup userGroup, boolean confirm) {
        if (currentUser.hasRole(UserRole.ADMIN) || currentUser.hasAction(AccountAction.PERMANENT_DELETE_USER_GROUP)) {
            if (confirm) {
                return;
            }
            DepartmentTaskStatsDTO stats = taskRepository.findGroupUserStatsByGroup(userGroup.getId())
                    .orElseThrow(() -> new GroupUserNotFoundException(userGroup.getId()));
            Integer usersCount = accountRepository.countByUserGroupId(userGroup.getId());
            if (stats.totalTasks() == 0 && usersCount == 0 && !confirm) {
                return;
            }
            if (stats.totalTasks() > 0 && !confirm) {
                throw new AccessDeniedException("У группы имеются задачи сохраненные в базе!");
            }

            if (usersCount > 0 && !confirm) {
                throw new AccessDeniedException("У группы имеются активные пользователи в базе!");
            }
            throw new AccessDeniedException("У вас не прав на удаление группы сотрудников!");
        }
    }
}
