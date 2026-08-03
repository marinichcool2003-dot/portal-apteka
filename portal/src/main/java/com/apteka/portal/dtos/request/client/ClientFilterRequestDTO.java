package com.apteka.portal.dtos.request.client;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Фильтр поиска клиентов")
public record ClientFilterRequestDTO (
        @Schema(description = "Логин пользователя")
        String login,
        @Schema(description = "Номер телефона")
        String phoneNumber,
        @Schema(description = "Идентификатор группы пользователей")
        Integer groupId,
        @Schema(description = "ФИО пользователя")
        String fullName,
        @Schema(description = "Добавочный номер")
        String extensionNumber,
        @Schema(description = "Признак активности")
        Boolean isActive
){}
