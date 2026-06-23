package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AccountAction {
    AMBASSADOR("AMBASSADOR", "Создание новостей и обновление своих новостей внутри своего отдела"),
    SENIOR_AMBASSADOR("SENIOR_AMBASSADOR", "Создание новостей и обновление своих новостей на любые группы"),
    UPDATE_ALL_NEWS_IN_GROUP("UPDATE_ALL_NEWS_IN_GROUP", "Обновление любых новостей в своей группе"),
    DELETE_ALL_NEWS_IN_GROUP("DELETE_ALL_NEWS_IN_GROUP", "Удаление новостей в рамках своей группы"),

    CREATE_APTEKA("CREATE_APTEKA", "Создание учетных записей аптек"),
    UPDATE_APTEKA("UPDATE_APTEKA", "Изменение данных учетных записей аптек"),
    SAFE_DELETE_APTEKA("SAFE_DELETE_APTEKA", "Безопасное удаление учетной записи аптеки"),
    PERMANENT_DELETE_APTEKA("PERMANENT_DELETE_APTEKA", "Полное удаление учетной записи аптеки"),

    CREATE_MAIN_PAGE_LINK("CREATE_MAIN_PAGE_LINK", "Создание ссылок на главной странице"),
    UPDATE_MAIN_PAGE_LINK("UPDATE_MAIN_PAGE_LINK", "Обновление ссылок на главной странице"),
    DELETE_MAIN_PAGE_LINK("DELETE_MAIN_PAGE_LINK", "Удаление ссылок на главной странице"),

    CREATE_CLIENT_IN_GROUP("CREATE_CLIENT_IN_GROUP", "Создание учетной записи сотрудника в своей группе"),
    CREATE_CLIENT_GRAND("CREATE_CLIENT", "Создание учетной записи сотрудника в любой группе"),
    UPDATE_CLIENT_IN_GROUP("UPDATE_CLIENT_IN_GROUP", "Обновление учетной записи сотрудника в своей группе"),
    UPDATE_CLIENT_GRAND("UPDATE_CLIENT_GRAND", "Обновление учетной записи сотрудника в любой группе"),
    SAFE_DELETE_CLIENT("SAFE_DELETE_CLIENT", "Безопасное удаление учетной записи сотрудника"),
    PERMANENT_DELETE_CLIENT("PERMANENT_DELETE_CLIENT", "Полное удаление учетной записи сотрудника");

    

    private final String code;
    private final String description;
}
