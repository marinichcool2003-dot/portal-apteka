package com.apteka.portal.dtos.request.apteka;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.Set;

@Schema(description = "Фильтр поиска аптек")
public record AptekaFilterRequestDTO(
        @Schema(description = "Логин пользователя")
        String login,
        @Schema(description = "Идентификаторы групп пользователей")
        Set<@Positive(message = "Номер группы может быть только положительным числом")Integer> userGroupsIds,
        @Schema(description = "Номер аптеки")
        @Positive(message = "Номер аптеки может быть только положительным числом") Integer number,
        @Schema(description = "Номер телефона")
        String phoneNumber,
        @Schema(description = "Город")
        String city,
        @Schema(description = "Улица")
        String street) {
}
