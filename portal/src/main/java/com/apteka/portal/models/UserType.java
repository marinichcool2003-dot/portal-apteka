package com.apteka.portal.models;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Тип учётной записи пользователя")
public enum UserType {
    @Schema(description = "Сотрудник")
    CLIENT,
    @Schema(description = "Аптека")
    APTEKA
}
