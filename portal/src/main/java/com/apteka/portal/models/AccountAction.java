package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowActionException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountAction {
    NEWS_WORK("NEWS_WORK", "Создание новостей и обновление своих новостей внутри своего отдела, а также удаление своих новостей"),
    NEWS_WORK_ALL_GROUPS("NEWS_WORK_ALL_GROUPS", "Создание новостей и обновление своих новостей на любые группы, а также удаление своих новостей в других группах"),
    UPDATE_ALL_NEWS_IN_GROUP("UPDATE_ALL_NEWS_IN_GROUP", "Обновление любых новостей в своей группе"),
    UPDATE_ALL_NEWS_CREATE_GROUP("UPDATE_ALL_NEWS_CREATE_GROUP", "Обновление любых новостей созданных сотрудниками вашей группы"),
    UPDATE_ALL_NEWS("UPDATE_ALL_NEWS", "Обновление любых новостей"),
    DELETE_ALL_NEWS_CREATE_GROUP("DELETE_ALL_NEWS_CREATE_GROUP", "Удаление любых новостей созданных сотрудниками вашей группы"),
    DELETE_ALL_NEWS_IN_GROUP("DELETE_ALL_NEWS_IN_GROUP", "Удаление любых новостей в рамках своей группы"),
    DELETE_ALL_NEWS("DELETE_ALL_NEWS", "Удаление любых новостей"),

    CREATE_APTEKA("CREATE_APTEKA", "Создание учетных записей аптек"),
    UPDATE_ALL_APTEKA("UPDATE_APTEKA", "Изменение всех данных учетных записей аптек"),
    UPDATE_APTEKA_ACCOUNT("UPDATE_APTEKA_ACCOUNT", "Изменение аккаунта и данных для входа аптеки"),
    UPDATE_APTEKA_DESCRIPTION("UPDATE_APTEKA_DESCRIPTION", "Изменение описания аптек"),
    SAFE_DELETE_APTEKA("SAFE_DELETE_APTEKA", "Безопасное удаление учетной записи аптеки"),
    PERMANENT_DELETE_APTEKA("PERMANENT_DELETE_APTEKA", "Полное удаление учетной записи аптеки или безопасное при необходимости"),

    CREATE_MAIN_PAGE_LINK("CREATE_MAIN_PAGE_LINK", "Создание ссылок на главной странице"),
    UPDATE_MAIN_PAGE_LINK("UPDATE_MAIN_PAGE_LINK", "Обновление ссылок на главной странице"),
    DELETE_MAIN_PAGE_LINK("DELETE_MAIN_PAGE_LINK", "Удаление ссылок на главной странице"),

    CREATE_CLIENT_IN_GROUP("CREATE_CLIENT_IN_GROUP", "Создание учетной записи сотрудника в своей группе"),
    CREATE_CLIENT_GRAND("CREATE_CLIENT", "Создание учетной записи сотрудника в любой группе"),
    UPDATE_CLIENT_IN_GROUP("UPDATE_CLIENT_IN_GROUP", "Обновление учетной записи сотрудника в своей группе"),
    UPDATE_CLIENT_GRAND("UPDATE_CLIENT_GRAND", "Обновление учетной записи сотрудника в любой группе"),
    SAFE_DELETE_CLIENT("SAFE_DELETE_CLIENT", "Безопасное удаление учетной записи сотрудника"),
    PERMANENT_DELETE_CLIENT("PERMANENT_DELETE_CLIENT", "Полное удаление учетной записи сотрудника"),

    CREATE_TASK_TO_GROUP("CREATE_TASK_TO_GROUP", "Создание задачи на любую группу сотрудников"),
    CREATE_TASK_GRAND("CREATE_TASK_GRAND", "Создание задачи на любую группу сотрудников и на любого сотрудника из любого отдела"),
    UPDATE_TASK_ASSIGNER_IN_GROUP("UPDATE_TASK_ASSIGNER_IN_GROUP", "Изменение исполнителя задачи любого сотрудника в своей группе"),
    UPDATE_TASK_ASSIGNER("UPDATE_TASK_ASSIGNER", "Изменение исполнителя задачи любого сотрудника");

    private final String code;
    private final String description;

    public static AccountAction fromCode(String code) {
        for (AccountAction action : AccountAction.values()) {
            if (action.code.equals(code)) {
                return action;
            }
        }

        throw new UnknowActionException("Неизвестный код действия " + code);
    }
}
