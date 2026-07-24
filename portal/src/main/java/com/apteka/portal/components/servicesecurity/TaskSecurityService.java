package com.apteka.portal.components.servicesecurity;

import com.apteka.portal.repository.GroupGroupVisibilityRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.WorkType;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TaskSecurityService {
    private final GroupGroupVisibilityRepository groupGroupVisibilityRepository;
    private final IsActiveValidator isActiveValidator;

    public void canSelectTask(Task task, AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_ANOTHER_GROUP_TASKS)) {
            return;
        }

        UserGroup creatorUserGroup = task.getWorkType().getGroupTask().getCreatorGroup();
        UserGroup assignerUserGroup = task.getWorkType().getGroupTask().getIntendedGroup();
        UserGroup userGroup = currentUser.getUserGroup();

        boolean existsRelationCreator = groupGroupVisibilityRepository.existsRelationBidirectional(userGroup.getId(),
                creatorUserGroup.getId());
        boolean existsRelationAssigner = groupGroupVisibilityRepository.existsRelationBidirectional(userGroup.getId(),
                assignerUserGroup.getId());

        if (existsRelationAssigner || existsRelationCreator) {
            return;
        }

        throw new AccessDeniedException("вы не можете просмотреть данную задачу");
    }

    public void validateCanSelectAllTaskStats(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAction(AccountAction.CAN_SELECT_ANOTHER_GROUP_TASKS)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на просмотр статистики всех групп");
    }

    public void validateCanSelectGroupTaskStats(Integer userGroupId, AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)
                || currentUser.hasAction(AccountAction.CAN_SELECT_ANOTHER_GROUP_TASKS)
                || Objects.equals(currentUser.getUserGroup().getId(), userGroupId)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на просмотр статистики этой группы");
    }

    public void validateCanCreateTask(Account assigner, WorkType workType, AppUserDetails currentUser) {
        if (!isActiveValidator.isWorkTypeActive(workType)) {
            throw new AccessDeniedException("Нельзя создать задачу с неактивным видом работ");
        }

        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER_GRAND)) {
            return;
        }

        GroupTask groupTask = workType.getGroupTask();

        if (canAddThisWorkType(workType, currentUser)) {

            if (!sameGroup(groupTask.getCreatorGroup(), currentUser)) {
                if (assigner != null) {
                    if (!canAssignedTo(assigner, groupTask)) {
                        throw new AccessDeniedException(
                                "Указанный исполнитель неактивен или его группа не связана с этим типом задач");
                    }
                    if (!currentUser.hasAction(AccountAction.CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER)) {
                        throw new AccessDeniedException(
                                "У вас нет прав создавать задачи на конкретного исполнителя в другую группу!");
                    }
                    return;
                } else {
                    if (!currentUser.hasAction(AccountAction.CAN_CREATE_TASK_ANOTHER_GROUP)) {
                        throw new AccessDeniedException("У вас нет прав на создание задач в другие группы!");
                    }
                    return;
                }
            }
            return;
        }
        throw new AccessDeniedException("У вас нет прав на создание данной задачи");
    }

    public void valdiateCanUpdateDescriptionTask(Task task, AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        validateIsClosed(task, currentUser);
        if (currentUser.hasAction(AccountAction.CAN_UPDATE_ALL_TASK)) {
            return;
        }
        throw new AccessDeniedException("Вы не можете изменить описание уже созданной задачи!");
    }

    public void canChangeAssigner(Task task, WorkType workType, Account assigner, AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (assigner != null && !isActiveValidator.isAccountActive(assigner)) {
            throw new AccessDeniedException("Выбранный исполнитель неактивен!");
        }

        boolean isAssigner = isAssigner(task, currentUser);
        boolean isTaskAssignedYourGroup = isTaskInYourGroup(task, currentUser);

        boolean isNewAssignerInYourGroup = (assigner != null) && sameGroup(assigner.getUserGroup(), currentUser);
        boolean isNewWorkTypeInTargetGroup = sameGroup(workType.getGroupTask().getCreatorGroup(), currentUser);
        boolean isCreator = isCreator(task, currentUser);

        if (isAssigner && isCreator) {
            throw new AccessDeniedException("Вы не можете переводить задачи, которые назначены на Вас и созданы вами");
        }

        if (isAssigner) {
            if (isNewWorkTypeInTargetGroup) {
                if (assigner == null || isNewAssignerInYourGroup) {
                    return;
                }
            }

            if (assigner == null && !isNewWorkTypeInTargetGroup) {
                if (!currentUser.hasAction(AccountAction.CAN_CHANGE_ASSIGNER_ASSIGNED_YOU_ANOTHER_GROUP)) {
                    throw new AccessDeniedException(
                            "У вас нет прав переводить назначенную вам задачу на другую группу!");
                }
                return;
            }

            if (assigner != null && !isNewWorkTypeInTargetGroup) {
                boolean newAssignerIsCreator = isCreator(task, currentUser);
                if (newAssignerIsCreator) {
                    throw new AccessDeniedException("Вы не можете перевести задачу на её же создателя!");
                }
                if (!canAssignedTo(assigner, workType.getGroupTask())) {
                    throw new AccessDeniedException("Выбранный исполнитель не связан с этим типом задач!");
                }
                if (!currentUser.hasAction(AccountAction.CAN_CHANGE_ASSIGNER_ASSIGNED_YOU_ANOTHER_GROUP_TO_ASSIGNER)) {
                    throw new AccessDeniedException(
                            "У вас нет прав переводить свою задачу на конкретного сотрудника другой группы!");
                }
                return;
            }
        }

        if (isTaskAssignedYourGroup && !isAssigner) {
            if (isNewWorkTypeInTargetGroup && isNewAssignerInYourGroup) {
                if (!currentUser.hasAction(AccountAction.CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_IN_GROUP)) {
                    throw new AccessDeniedException("У вас нет прав переназначать задачи коллег внутри группы!");
                }
                return;
            }

            if (assigner != null && !isNewWorkTypeInTargetGroup) {
                if (!canAssignedTo(assigner, workType.getGroupTask())) {
                    throw new AccessDeniedException("Выбранный исполнитель не связан с этим типом задач!");
                }
                if (!currentUser.hasAction(AccountAction.CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_ANOTHER_GROUP)) {
                    throw new AccessDeniedException(
                            "У вас нет прав переводить задачи коллег на сотрудников другой группы!");
                }
                return;
            }

            if (assigner == null && !isNewWorkTypeInTargetGroup) {
                if (!currentUser.hasAction(AccountAction.CAN_CHANGE_ASSIGNER_GROUP_FROM_YOUR_GROUP)) {
                    throw new AccessDeniedException("У вас нет прав переводить задачи вашей группы на другие группы!");
                }
                return;
            }
        }

        throw new AccessDeniedException(
                "Вы не можете менять тип работ и исполнителя задачи, к которой не имеете отношения!");
    }

    public void validateChangeStatusInTask(Task task, AppUserDetails currentUser, TaskStatus newStatus) {

        if (task.getStatus() == newStatus) {
            throw new AccessDeniedException("Нельзя поменять статус задачи на идентичный!");
        }

        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        boolean isActive = Objects.equals(task.getStatus(), TaskStatus.OPEN)
                || Objects.equals(task.getStatus(), TaskStatus.PROCESSED);

        if (!isActive) {
            Instant now = Instant.now();
            Instant twoWeeksAgo = now.minus(14, ChronoUnit.DAYS);

            if (task.getClosingDate() == null || task.getClosingDate().isBefore(twoWeeksAgo)) {
                throw new AccessDeniedException(
                        "Нельзя изменить статус задачи, с момента закрытия/отклонения которой прошло более 2 недель!");
            }
        }

        if (task.getStatus() == TaskStatus.DENIED) {
            throw new AccessDeniedException("Нельзя изменять статус у отклоненных задач!");
        }

        boolean isCreator = isCreator(task, currentUser);
        boolean isAssigner = isAssigner(task, currentUser);

        boolean canChangeInGroup = (task.getAssigner() == null
                && isTaskInYourGroup(task, currentUser)
                && currentUser.hasAnyAction(
                        AccountAction.CAN_CHANGE_STATUS_TASK_IN_GROUP,
                        AccountAction.CAN_CHANGE_STATUS_TASK_ASSIGNED_IN_GROUP))
                ||
                (isTaskInYourGroup(task, currentUser)
                        && currentUser.hasAction(AccountAction.CAN_CHANGE_STATUS_TASK_ASSIGNED_IN_GROUP));

        if (isAssigner || isCreator || canChangeInGroup) {
            return;
        }

        throw new AccessDeniedException("Вы не можете изменить статус данной задачи!");
    }

    public void validateCanChangeTitle(Task task, AppUserDetails currentUser, String newTitle) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        validateIsClosed(task, currentUser);

        if (currentUser.hasAction(AccountAction.CAN_UPDATE_ALL_TASK)) {
            return;
        }

        boolean isAssigner = isAssigner(task, currentUser);
        boolean isNotRelatedTask = task.getAssigner() == null;
        boolean isTaskInYourGroup = isTaskInYourGroup(task, currentUser);
        boolean fullChangeTitle = !task.getTitle().contains(newTitle);
        boolean hasActionUpdateBeforeAssigned = currentUser.hasAction(AccountAction.CAN_ADD_TITLE_BEFORE_ASSIGNED);

        if (fullChangeTitle && !currentUser.hasAction(AccountAction.CAN_FULL_UPDATE_TITLE_BEFORE_ASSIGNED)) {
            throw new AccessDeniedException(
                    "Вы не можете полностью изменять заголовок задачи! (имеется возможность дописать)");
        }

        if (isTaskInYourGroup) {
            if (isNotRelatedTask) {
                if (!hasActionUpdateBeforeAssigned) {
                    return;
                }
                throw new AccessDeniedException("Вы не можете изменять заголовок ещё не распределённых задач!");
            }
            if (!isAssigner) {
                throw new AccessDeniedException("Вы не можете изменять заголовок задач которые не назначены вам!");
            }
        }
        throw new AccessDeniedException("Вы не можете изменить заголовок данной задачи!");
    }

    public void validateCanPermanentDeleteTask(AppUserDetails currentUser) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_PERMANENT_DELETE_TASK)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на удаление задач!");
    }

    private void validateIsClosed(Task task, AppUserDetails currentUser) {
        if (task.getStatus() == TaskStatus.CLOSED || task.getStatus() == TaskStatus.DENIED) {
            throw new AccessDeniedException(
                    "Вы не можете делать какие-либо изменения в закрытых или отклоненных задачах!");
        }
    }

    private boolean canAssignedTo(Account account, GroupTask groupTask) {
        if (!isActiveValidator.isAccountActive(account)) {
            return false;
        }
        boolean isCorrectCreatorGroup = Objects.equals(account.getUserGroup().getId(),
                groupTask.getCreatorGroup().getId());

        return isCorrectCreatorGroup;
    }

    private boolean canAddThisWorkType(WorkType workType, AppUserDetails currentUser) {
        return Objects.equals(workType.getGroupTask().getIntendedGroup().getId(), currentUser.getUserGroup().getId());
    }

    private boolean sameGroup(UserGroup userGroup, AppUserDetails currentUser) {
        return Objects.equals(userGroup.getId(), currentUser.getUserGroup().getId());
    }

    private boolean isTaskInYourGroup(Task task, AppUserDetails currentUser) {
        return Objects.equals(task.getWorkType().getGroupTask().getCreatorGroup().getId(),
                currentUser.getUserGroup().getId());
    }

    private boolean isCreator(Task task, AppUserDetails currentUser) {
        return Objects.equals(task.getCreator().getId(), currentUser.getInternalId());
    }

    private boolean isAssigner(Task task, AppUserDetails currentUser) {
        return Objects.equals(task.getAssigner().getId(), currentUser.getInternalId());
    }
}