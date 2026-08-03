package com.apteka.portal.dtos.response.usergroup;

import java.util.List;

import com.apteka.portal.models.UserGroupType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Группа пользователей с видимыми группами")
public record UserGroupWithVisibleResponseDTO(
        @Schema(description = "Идентификатор группы")
        Integer id,
        @Schema(description = "Наименование группы")
        String name,
        @Schema(description = "Номер телефона")
        String phoneNumber,
        @Schema(description = "Внутренний номер")
        String internalNumber,
        @Schema(description = "Добавочный номер")
        String extensionNumber,
        @Schema(description = "URL аватара")
        String avatarUrl,
        @Schema(description = "Признак активности")
        Boolean isActive,
        // AUDIT-FIX: тип группы
        @Schema(description = "Тип группы пользователей")
        UserGroupType groupType,
        @Schema(description = "Список видимых групп")
        List<UserGroupShortResponseDTO> visibleGroups
) {}
