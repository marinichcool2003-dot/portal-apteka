package com.apteka.portal.components.servicesecurity;

import java.util.Objects;
import java.util.Set;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import com.apteka.portal.components.validators.IsActiveValidator;
import com.apteka.portal.exceptions.SelfDeleteException;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;
import com.apteka.portal.models.AccountAction.LevelAction;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class ClientSecurityService {

    private final IsActiveValidator isActiveValidator;

    public void validateWhoCanSelectClients(AppUserDetails currentUser) {
        if (currentUser.getType() != UserType.CLIENT) {
            throw new AccessDeniedException("У вас нет прав на просмотр данных сотрудников!");
        }
    }

    public void validateWhoCanSelectNonActiveClients(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_NON_ACTIVE_CLIENT_GRAND)) {
            return;
        }
        if (userGroup == null) {
            throw new AccessDeniedException("Вы не можете видеть неактивных пользователей!");
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_NON_ACTIVE_CLIENT_IN_GROUP)
                && sameGroup(currentUser, userGroup)) {
            return;
        }
        throw new AccessDeniedException("Вы не можете видеть неактивных пользователей!");
    }

    public void validateWhoCanSelectClientStats(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_CLIENT_STATS_GRAND)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_SELECT_CLIENT_STATS_IN_GROUP)
                && sameGroup(currentUser, userGroup)) {
            return;
        }
        throw new AccessDeniedException("Вы не можете просматривать статистику пользователей!");
    }

    public void validateCanCreateClient(AppUserDetails currentUser, UserGroup userGroup) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.getType() != UserType.CLIENT) {
            throw new AccessDeniedException("У вас нет прав на создание сотрудников!");
        }
        if (currentUser.hasAction(AccountAction.CREATE_CLIENT_GRAND)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CREATE_CLIENT_IN_GROUP) && sameGroup(currentUser, userGroup)) {
            return;
        }
        throw new AccessDeniedException("У вас нет прав на создание сотрудников!");
    }

    public void validateCanUpdateClientAccount(AppUserDetails currentUser, Account account) {
        baseUpdateClientValidator(currentUser, account);
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_GRAND)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_ACCOUNT_GRAND)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_ACCOUNT_IN_GROUP)
                && sameGroup(currentUser, account.getUserGroup())) {
            return;
        }

        throw new AccessDeniedException("У вас нет прав на изменение чужих учетных записей аккаунта!");
    }

    public void validateCanUpdateClientDescription(AppUserDetails currentUser, Client client) {
        Account account = client.getAccount();
        baseUpdateClientValidator(currentUser, account);
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_GRAND)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_DESCRIPTION_GRAND)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.UPDATE_CLIENT_DESCRIPTION_IN_GROUP)
                && sameGroup(currentUser, account.getUserGroup())) {
            return;
        }

        throw new AccessDeniedException("У вас нет прав на изменение чужих данных пользователя!");
    }

    public void validateCanUpdateFullClient(AppUserDetails currentUser, Account account) {
        baseUpdateClientValidator(currentUser, account);
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (!currentUser.hasAction(AccountAction.UPDATE_CLIENT_GRAND)) {
            throw new AccessDeniedException("У вас нет прав на полное обновление учетной записи сотрудника!");
        }
    }

    private void baseUpdateClientValidator(AppUserDetails currentUser, Account account) {
        criticalAccessValidator(currentUser, account);
        if (account.getUserRole() == UserRole.BOSS) {
            throw new AccessDeniedException("Только администратор может изменять учетные записи начальников отдела!");
        }
    }

    private void criticalAccessValidator(AppUserDetails currentUser, Account account) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.getType() != UserType.CLIENT) {
            throw new AccessDeniedException("У вас нет прав на изменение сотрудников!");
        }
        if (account.getUserRole() == UserRole.ADMIN) {
            throw new AccessDeniedException("Никто не может изменять учётную запись администратора!");
        }
        if (!isActiveValidator.isAccountActive(account)) {
            throw new AccessDeniedException("Вы не можете обновить удалённый аккаунт. Обратитесь к администратору!");
        }
    }

    public void canAddActions(Set<AccountAction> actions, AppUserDetails currentUser, Account account) {

        for (AccountAction accountAction : actions) {
            if (account.getActions().contains(accountAction)) {
                throw new AccessDeniedException("У пользователя уже есть право на действие: " + accountAction);
            }
        }

        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }

        if (currentUser.hasAction(AccountAction.CAN_ADD_ACCOUNT_ACTIONS_GRAND_EXTENDED)) {
            criticalAccessValidator(currentUser, account);
            return;
        }

        baseUpdateClientValidator(currentUser, account);
        boolean hasActionGrand = currentUser.hasAction(AccountAction.CAN_ADD_ACCOUNT_ACTIONS_GRAND);
        boolean hasActionInGroup = currentUser.hasAction(AccountAction.CAN_ADD_ACCOUNT_ACTIONS_IN_GROUP);

        if (!hasActionGrand && !hasActionInGroup) {
            throw new AccessDeniedException("У вас нет прав на добавление действий для аккаунтов!");
        }

        if (!hasActionGrand && hasActionInGroup) {
            if (!sameGroup(currentUser, account.getUserGroup())) {
                throw new AccessDeniedException(
                        "Вы можете изменять аккаунты сотрудников только в рамках своей группы!");
            }
        }

        LevelAction maxAllowedLevel = LevelAction.LOW;

        if (currentUser.hasRole(UserRole.BOSS)) {
            maxAllowedLevel = LevelAction.MEDIUM;
        }

        for (AccountAction accountAction : actions) {
            if (AccountAction.getLevelValue(accountAction) > maxAllowedLevel.level()) {
                throw new AccessDeniedException(
                        "У вас нет права присваивать данное действие (превышен уровень доступа)!");
            }
        }
    }

    public void canRemoveActions(Set<AccountAction> actions, AppUserDetails currentUser, Account account) {
        for (AccountAction accountAction : actions) {
            if (!account.getActions().contains(accountAction)) {
                throw new AccessDeniedException("У пользователя нет действия: " + accountAction);
            }
        }
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_REMOVE_ACCOUNT_ACTIONS_GRAND_EXTENDED)) {
            criticalAccessValidator(currentUser, account);
            return;
        }
        baseUpdateClientValidator(currentUser, account);
        boolean hasActionGrand = currentUser.hasAction(AccountAction.CAN_REMOVE_ACCOUNT_ACTIONS_GRAND);
        boolean hasActionInGroup = currentUser.hasAction(AccountAction.CAN_REMOVE_ACCOUNT_ACTIONS_IN_GROUP);

        if (!hasActionGrand && !hasActionInGroup) {
            throw new AccessDeniedException("У вас нет прав на удаление разрешенных действий сотрудникам");
        }
        if (!hasActionGrand && hasActionInGroup) {
            if (!sameGroup(currentUser, account.getUserGroup())) {
                throw new AccessDeniedException(
                        "Вы можете изменять аккаунты сотрудников только в рамках своей группы!");
            }
        }

        LevelAction maxLevel = LevelAction.LOW;

        if (currentUser.hasRole(UserRole.BOSS)) {
            maxLevel = LevelAction.MEDIUM;
        }

        for (AccountAction action : actions) {
            if (AccountAction.getLevelValue(action) > maxLevel.level()) {
                throw new AccessDeniedException("У вас нет права удалять данное действие (превышен уровень доступа)!");
            }
        }
    }

    public void canGiveRole(AppUserDetails currentUser, UserRole role) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (role == UserRole.ADMIN) {
            throw new AccessDeniedException("Только администратор может создать другого администратора!");
        }
        if (currentUser.hasAction(AccountAction.CAN_GIVE_ROLE_CLIENT_GRAND)) {
            return;
        }
        if (currentUser.getRole().getLevel() <= role.getLevel()) {
            throw new AccessDeniedException("Вы не можете присвоить роль выше или равную своей!");
        }
    }

    public void canSaveDelete(AppUserDetails currentUser, Account account) {
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.PERMANENT_DELETE_CLIENT)) {
            return;
        }

        if (!isActiveValidator.isAccountActive(account)) {
            throw new AccessDeniedException("Аккаунт уже не активен!");
        }

        if (Objects.equals(currentUser.getInternalId(), account.getId())) {
            throw new SelfDeleteException("Вы не можете удалить сами себя!");
        }

        boolean hasActionGrand = currentUser.hasAction(AccountAction.SAFE_DELETE_CLIENT_GRAND);
        boolean hasActionInGroup = currentUser.hasAction(AccountAction.SAFE_DELETE_CLIENT_IN_GROUP);

        if (!hasActionGrand && !hasActionInGroup) {
            throw new AccessDeniedException("У вас нет права удалять сотрудников!");
        }

        if (hasActionGrand && !hasActionInGroup) {
            return;
        }

        if (!hasActionGrand && hasActionInGroup) {
            if (!sameGroup(currentUser, account.getUserGroup())) {
                throw new AccessDeniedException("У вас нет права на удаление сотрудника другой группы!");
            }
        }
    }

    public void activateAfterSafeDelete(AppUserDetails currentUser, Account account) {
        if (isActiveValidator.isAccountActive(account)) {
            throw new AccessDeniedException("Пользователь уже активен");
        }
        if (currentUser.hasRole(UserRole.ADMIN)) {
            return;
        }
        if (currentUser.hasAction(AccountAction.CAN_ACTIVATE_CLIENT_AFTER_SAFE_DELETE)) {
            return;
        }
        throw new AccessDeniedException("У вас нет права восстанавливать пользователя после удаления!");
    }

    public void canPermanentDelete(AppUserDetails currentUser, Account account) {
        if (!currentUser.hasRole(UserRole.ADMIN) && !currentUser.hasAction(AccountAction.PERMANENT_DELETE_CLIENT)) {
            throw new AccessDeniedException("У вас нет права на удаление сотрудников!");
        }
        if (Objects.equals(currentUser.getInternalId(), account.getId())) {
            throw new SelfDeleteException("Вы не можете удалить сами себя!");
        }
    }

    private boolean sameGroup(AppUserDetails currentUser, UserGroup targetGroup) {
        return Objects.equals(currentUser.getUserGroup().getId(), targetGroup.getId());
    }
}