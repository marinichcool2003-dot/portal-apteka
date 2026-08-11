package com.apteka.portal.dtos.response;

import com.apteka.portal.models.AccountAction;
import com.apteka.portal.models.AccountAction.LevelAction;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Действие (право), которое можно назначить другому пользователю")
public record AccountActionResponseDTO(
        @Schema(description = "Код действия", example = "CAN_DISTRIBUTE_TASK_IN_GROUP")
        String code,
        @Schema(description = "Описание действия")
        String description,
        @Schema(description = "Уровень критичности")
        LevelAction level
) {
    public static AccountActionResponseDTO from(AccountAction action) {
        return new AccountActionResponseDTO(action.getCode(), action.getDescription(), action.getLevel());
    }
}
