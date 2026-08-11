package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Тип группы пользователей")
@AllArgsConstructor
@Getter
public enum UserGroupType {
    @Schema(description = "Группа аптек")
    APTEKA_GROUP("APTEKA_GROUP", "Группа аптек", "Организационная группа аптек"),

    @Schema(description = "Группа сотрудников")
    EMPLOYEE_GROUP("EMPLOYEE_GROUP", "Группа сотрудников", "Отдел / группа сотрудников");

    private final String code;
    private final String name;
    private final String description;
}
