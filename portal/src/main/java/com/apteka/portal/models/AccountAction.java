package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowActionException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountAction {
    NEWS_WORK("NEWS_WORK", "Создание новостей и обновление своих новостей внутри своего отдела, а также удаление своих новостей", LevelAction.MEDIUM),
    NEWS_WORK_ALL_GROUPS("NEWS_WORK_ALL_GROUPS", "Создание новостей и обновление своих новостей на любые группы, а также удаление своих новостей в других группах", LevelAction.HIGH),
    UPDATE_ALL_NEWS_IN_GROUP("UPDATE_ALL_NEWS_IN_GROUP", "Обновление любых новостей в своей группе", LevelAction.MEDIUM),
    UPDATE_ALL_NEWS_CREATE_GROUP("UPDATE_ALL_NEWS_CREATE_GROUP", "Обновление любых новостей созданных сотрудниками вашей группы", LevelAction.MEDIUM),
    UPDATE_ALL_NEWS("UPDATE_ALL_NEWS", "Обновление любых новостей", LevelAction.HIGH),
    DELETE_ALL_NEWS_CREATE_GROUP("DELETE_ALL_NEWS_CREATE_GROUP", "Удаление любых новостей созданных сотрудниками вашей группы", LevelAction.MEDIUM),
    DELETE_ALL_NEWS_IN_GROUP("DELETE_ALL_NEWS_IN_GROUP", "Удаление любых новостей в рамках своей группы", LevelAction.MEDIUM),
    DELETE_ALL_NEWS("DELETE_ALL_NEWS", "Удаление любых новостей", LevelAction.CRITICAL),

    CREATE_APTEKA("CREATE_APTEKA", "Создание учетных записей аптек", LevelAction.MEDIUM),
    UPDATE_ALL_APTEKA("UPDATE_APTEKA", "Изменение всех данных учетных записей аптек", LevelAction.HIGH),
    UPDATE_APTEKA_ACCOUNT("UPDATE_APTEKA_ACCOUNT", "Изменение аккаунта и данных для входа аптеки", LevelAction.HIGH),
    UPDATE_APTEKA_DESCRIPTION("UPDATE_APTEKA_DESCRIPTION", "Изменение описания аптек", LevelAction.MEDIUM), 
    SAFE_DELETE_APTEKA("SAFE_DELETE_APTEKA", "Безопасное удаление учетной записи аптеки", LevelAction.MEDIUM),
    PERMANENT_DELETE_APTEKA("PERMANENT_DELETE_APTEKA", "Полное удаление учетной записи аптеки или безопасное при необходимости", LevelAction.CRITICAL),

    CAN_SELECT_NON_ACTIVE_MAIN_PAGE_LINK("CAN_SELECT_NON_ACTIVE_MAIN_PAGE_LINK", "Возможность просмотреть неактивные ссылки", LevelAction.HIGH),
    CREATE_MAIN_PAGE_LINK("CREATE_MAIN_PAGE_LINK", "Создание ссылок на главной странице", LevelAction.MEDIUM),
    UPDATE_MAIN_PAGE_LINK("UPDATE_MAIN_PAGE_LINK", "Обновление ссылок на главной странице", LevelAction.HIGH),
    SAFE_DELETE_MAIN_PAGE_LINK("SAFE_DELETE_MAIN_PAGE_LINK", "Безопасное удаление ссылок на главной странице", LevelAction.MEDIUM),
    CAN_ACTIVATE_MAIN_PAGE_LINK_AFTER_SAFE_DELETE("CAN_ACTIVATE_MAIN_PAGE_LINK_AFTER_SAFE_DELETE", "Восстановление после безопасного удаление ссылок на главной странице", LevelAction.HIGH),
    PERMANENT_DELETE_MAIN_PAGE_LINK("PERMANENT_DELETE_MAIN_PAGE_LINK", "Удаление ссылок на главной странице", LevelAction.HIGH),

    // CAN_USE_BASE_CLIENT_FILTER("CAN_USE_BASE_FILTER", "Возможность использовать расширенный фильтр по группам", LevelAction.LOW),
    // CAN_USE_ALL_CLIENT_FILTER("CAN_USE_ALL_CLIENT_FILTER", "Возможность использовать полный фильтр", LevelAction.MEDIUM),

    CAN_SELECT_NON_ACTIVE_CLIENT_IN_GROUP("CAN_SELECT_NON_ACTIVE_CLIENT_IN_GROUP", "Возможность видеть неактивных пользователей в своей группе", LevelAction.LOW),
    CAN_SELECT_NON_ACTIVE_CLIENT_GRAND("CAN_SELECT_NON_ACTIVE_CLIENT_GRAND", "Возможность видеть неактивных пользователей в любой группе", LevelAction.HIGH),
    CAN_SELECT_CLIENT_STATS_IN_GROUP("CAN_SELECT_CLIENT_STATS_IN_GROUP", "Возможность просматривать статистику пользователя в общей группе", LevelAction.LOW),
    CAN_SELECT_CLIENT_STATS_GRAND("CAN_SELECT_CLIENT_STATS_GRAND", "Возможность просматривать статистику пользователя в любой группе", LevelAction.MEDIUM),
    CREATE_CLIENT_IN_GROUP("CREATE_CLIENT_IN_GROUP", "Создание учетной записи сотрудника в своей группе", LevelAction.MEDIUM),
    CREATE_CLIENT_GRAND("CREATE_CLIENT", "Создание учетной записи сотрудника в любой группе", LevelAction.HIGH),
    CAN_GIVE_ROLE_CLIENT_GRAND("CAN_GIVE_ROLE_CLIENT_GRAND", "Добавление роли сотруднику возможность добавить роль выше своей, (кроме ADMIN)", LevelAction.CRITICAL),
    CAN_ADD_ACCOUNT_ACTIONS_IN_GROUP("CAN_ADD_ACCOUNT_ACTIONS_IN_GROUP", "Возможность добавления действий для учетной записи сотруднику своей группы кроме начальников в рамках уровня действия", LevelAction.HIGH),
    CAN_ADD_ACCOUNT_ACTIONS_GRAND("CAN_ADD_ACCOUNT_ACTIONS_GRAND", "Возможность добавления действий для учетной записи любому сотруднику кроме начальников в рамках уровня действия", LevelAction.HIGH),
    CAN_ADD_ACCOUNT_ACTIONS_GRAND_EXTENDED("CAN_ADD_ACCOUNT_ACTIONS_GRAND_EXTENDED", "Возможность добавления действий для учетной записи любому сотруднику без ограничений, но только на активные аккаунты", LevelAction.CRITICAL),
    CAN_REMOVE_ACCOUNT_ACTIONS_IN_GROUP("CAN_REMOVE_ACCOUNT_ACTIONS_IN_GROUP", "Возможность удаления действий для учетной записи сотруднику своей группы кроме начальников в рамках уровня действия", LevelAction.HIGH),
    CAN_REMOVE_ACCOUNT_ACTIONS_GRAND("CAN_REMOVE_ACCOUNT_ACTIONS_IN_GROUP", "Возможность удаления действий для учетной записи любому сотруднику кроме начальников в рамках уровня действия", LevelAction.HIGH),
    CAN_REMOVE_ACCOUNT_ACTIONS_GRAND_EXTENDED("CAN_REMOVE_ACCOUNT_ACTIONS_GRAND_EXTENDED", "Возможность удаления действий для учетной записи любому сотруднику без ограничений, но только на активные аккаунты", LevelAction.CRITICAL),
    UPDATE_CLIENT_ACCOUNT_IN_GROUP("UPDATE_CLIENT_IN_GROUP", "Обновление учетной записи аккаунта сотрудника в своей группе", LevelAction.HIGH),
    UPDATE_CLIENT_ACCOUNT_GRAND("UPDATE_CLIENT_ACCOUNT_GRAND", "Обновление учетной записи аккаунта сотрудника в любой группе", LevelAction.HIGH),
    UPDATE_CLIENT_DESCRIPTION_IN_GROUP("UPDATE_CLIENT_DESCRIPTION_IN_GROUP", "Обновление описания учетной записи сотрудника в своей группе", LevelAction.MEDIUM),
    UPDATE_CLIENT_DESCRIPTION_GRAND("UPDATE_CLIENT_DESCRIPTION_GRAND", "Обновление описания учетной записи сотрудника в любой группе", LevelAction.HIGH),
    UPDATE_CLIENT_IN_GROUP_GRAND("UPDATE_CLIENT_IN_GROUP_GRAND", "Обновление всех данных сотрудников в группе", LevelAction.HIGH),
    UPDATE_CLIENT_GRAND("UPDATE_CLIENT_GRAND", "Обновление учетной записи сотрудника в любой группе", LevelAction.HIGH),
    SAFE_DELETE_CLIENT_IN_GROUP("SAFE_DELETE_CLIENT_IN_GROUP", "Безопасное удаление учетной записи сотрудника в общей группе", LevelAction.HIGH),
    SAFE_DELETE_CLIENT_GRAND("SAFE_DELETE_CLIENT_GRAND", "Безопасное удаление учетной записи любого сотрудника", LevelAction.CRITICAL),
    CAN_ACTIVATE_CLIENT_AFTER_SAFE_DELETE("CAN_ACTIVATE_CLIENT_AFTER_SAFE_DELETE", "Возвращение учетной записи пользователя после удаления", LevelAction.CRITICAL),
    PERMANENT_DELETE_CLIENT("PERMANENT_DELETE_CLIENT", "Полное удаление учетной записи сотрудника либо безопасное по выбору", LevelAction.CRITICAL),

    CAN_SELECT_ALL_NON_ACTIVE_GROUPS("CAN_SELECT_ALL_NON_ACTIVE_GROUPS", "Возможность просматривать все неактивные группы", LevelAction.HIGH),
    CAN_SELECT_ALL_ACTIVE_GROUPS("CAN_SELECT_ALL_ACTIVE_GROUPS", "Возможность просматривать все активные группы", LevelAction.MEDIUM),
    CAN_CREATE_USER_GROUP("CAN_CREATE_USER_GROUP", "Возможность создавать отделы", LevelAction.HIGH),
    CAN_UPDATE_SELF_USER_GROUP("CAN_UPDATE_SELF_USER_GROUP", "Возможность обновлять собственную группу", LevelAction.HIGH),
    CAN_UPDATE_USER_GROUP("CAN_UPDATE_USER_GROUP", "Возможность обновлять любую группу", LevelAction.CRITICAL),
    SAFE_DELETE_USER_GROUP("SAFE_DELETE_USER_GROUP", "Безопасное удаление группы пользователей", LevelAction.HIGH),
    CAN_ACTIVATE_USER_GROUP_AFTER_SAFE_DELETE("CAN_ACTIVATE_USER_GROUP_AFTER_SAFE_DELETE", "Восстановление группы пользователей после безопасного удаления", LevelAction.HIGH),
    PERMANENT_DELETE_USER_GROUP("PERMANENT_DELETE_USER_GROUP", "Полное удаление группы пользователей", LevelAction.CRITICAL),

    BASE_WORK_WITH_GROUP_TASK("BASE_WORK_WITH_GROUP_TASK","Базовая работа с типами задач (Включает (просмотр, создание, безопасное удаление, безопасное изменение, просмотр удалённых) всё в рамках своей группы)", LevelAction.MEDIUM),
    GRAND_WORK_WITH_GROUP_TASK("GRAND_WORK_WITH_GROUP_TASK","Расширенная работа с типами задач (Включает то же что и базовая только для всех групп)", LevelAction.HIGH),
    NON_SAFE_UPDATE_GROUP_TASK("NON_SAFE_UPDATE","Включает небезопасное обновление", LevelAction.CRITICAL),
    CAN_PERMANENT_DELETE_GROUP_TASK("CAN_PERMANENT_DELETE_GROUP_TASK", "Возможность перманентного удаления", LevelAction.CRITICAL),

    BASE_WORK_WITH_WORK_TYPE("BASE_WORK_WITH_WORK_TYPE","Базовая работа с видами работ (Включает (просмотр, создание, безопасное удаление, безопасное изменение, просмотр удалённых) всё в рамках своей группы)", LevelAction.MEDIUM),
    GRAND_WORK_WITH_WORK_TYPE("GRAND_WORK_WITH_WORK_TYPE","Расширенная работа с видами работ (Включает то же что и базовая только для всех групп)", LevelAction.HIGH),
    NON_SAFE_UPDATE_WORK_TYPE("NON_SAFE_UPDATE_WORK_TYPE","Включает небезопасное обновление", LevelAction.CRITICAL),
    CAN_PERMANENT_DELETE_WORK_TYPE("CAN_PERMANENT_DELETE_WORK_TYPE", "Возможность перманентного удаления", LevelAction.CRITICAL),

    CAN_DISTRIBUTE_TASK_IN_GROUP("CAN_DISTRIBUTE_TASK_IN_GROUP", "Возможность распределения задач в группе", LevelAction.LOW),
    CAN_CREATE_TASK_ANOTHER_GROUP("CAN_CREATE_TASK_ANOTHER_GROUP", "Возможность создавать задачи в другие группы", LevelAction.LOW),
    CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER("CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER", "Возможность создавать задачи в другую группу с которой можно работать на конкретного исполнителя", LevelAction.LOW),
    CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER_GRAND("CAN_CREATE_TASK_ANOTHER_GROUP_TO_ASSIGNER_GRAND", "Возможность создать задачи на конкретного сотрудника в любую группу", LevelAction.HIGH),
    CAN_CREATE_TASK_TO_APTEKI("CAN_CREATE_TASK_TO_APTEKI", "Возможность создавать задачи всем аптекам", LevelAction.MEDIUM),

    CAN_SELECT_USER_GROUP_STATS("CAN_SELECT_USER_GROUP_STATS", "Возможность видеть статистику по задачам в своей группе", LevelAction.LOW),
    CAN_SELECT_USER_GROUP_STATS_ALL_VISIBILITY("CAN_SELECT_USER_GROUP_STATS_ALL_VISIBILITY", "Возможность видеть статистику задач групп с которыми вы работаете", LevelAction.MEDIUM),
    CAN_SELECT_USER_GROUP_STATS_GRAND("CAN_SELECT_USER_GROUP_STATS_GRAND", "Возможность видеть статистику всех групп", LevelAction.HIGH),

    CAN_SELECT_ANOTHER_GROUP_TASKS("CAN_SELECT_ANOTHER_GROUP_TASKS", "Возможность просматривать задачи никак не связанные с вами и вашей группой", LevelAction.HIGH),

    CAN_CHANGE_ASSIGNER_ASSIGNED_YOU_ANOTHER_GROUP("CAN_CHANGE_ASSIGNER_ASSIGNED_YOU", "Возможность сменить исполнителя задачи, которая назначена тебе на другую группу", LevelAction.LOW),
    CAN_CHANGE_ASSIGNER_ASSIGNED_YOU_ANOTHER_GROUP_TO_ASSIGNER("CAN_CHANGE_ASSIGNER_ASSIGNED_YOU_ANOTHER_GROUP_TO_ASSIGNER", "Возможность изменить исполнителя задачи, которая назначена на тебя, на другого исполнителя любой доступной группы", LevelAction.LOW),
    CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_IN_GROUP("CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_IN_GROUP", "Возможность изменить исполнителя задачи назначенной не тебе (сотруднику твоей группы) на другого сотрудника твоей группы", LevelAction.LOW),
    CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_ANOTHER_GROUP("CAN_CHANGE_ASSIGNER_ASSIGNED_NOT_YOU_ANOTHER_GROUP", "Возможность изменить исполнителя задачи назначенной не тебе (сотруднику твоей группы) на другого сотрудника работающей с тобой группы", LevelAction.LOW),
    CAN_CHANGE_ASSIGNER_GROUP_FROM_YOUR_GROUP("CAN_CHANGE_ASSIGNER_GROUP_FROM_YOUR_GROUP", "Возможность перевести задачу со своей группы на группу работающую с вашей группой", LevelAction.LOW),

    CAN_CHANGE_STATUS_TASK_IN_GROUP("CAN_CHANGE_STATUS_TASK_IN_GROUP", "Возможность менять статус неназначенных задач в вашей группе", LevelAction.LOW),
    CAN_CHANGE_STATUS_TASK_ASSIGNED_IN_GROUP("CAN_CHANGE_STATUS_TASK_ASSIGNED_IN_GROUP", "Возможность менять статус назначенных задач в вашей группе", LevelAction.MEDIUM),
    
    CAN_ADD_TITLE_BEFORE_ASSIGNED("CAN_UPDATE_TITLE_BEFORE_ASSIGNED", "Возможность изменять заголовок задач до их распределения", LevelAction.LOW),
    CAN_FULL_UPDATE_TITLE_BEFORE_ASSIGNED("CAN_FULL_IPDATE_TITLE_BEFORE_ASSIGNED", "Возможность полностью изменять заголовок задач до распределения", LevelAction.MEDIUM),
    CAN_UPDATE_ALL_TASK("CAN_UPDATE_TASK", "Возможность полностью изменять задачи", LevelAction.HIGH),
    
    CAN_PERMANENT_DELETE_TASK("CAN_PERMANENT_DELETE_TASK", "Возможность полностью удалить задачу", LevelAction.HIGH);



    private final String code;
    private final String description;
    private final LevelAction level;

    @AllArgsConstructor
    public enum LevelAction {
        LOW(1),
        MEDIUM(2),
        HIGH(3),
        CRITICAL(4);
        private final int level;

        public int level() {
            return level;
        }
    }

    public static AccountAction fromCode(String code) {
        for (AccountAction action : AccountAction.values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }
        throw new UnknowActionException("Неизвестный код действия " + code);
    }

    public static int getLevelValue(AccountAction action) {
        return action.getLevel().level();
    }
}
