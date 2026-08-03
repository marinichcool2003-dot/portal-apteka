package com.apteka.portal.dtos.response.usergroup;

import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserGroupType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Краткий ответ по группе пользователей")
public record UserGroupShortResponseDTO(
    @Schema(description = "Идентификатор группы")
    Integer id,
    @Schema(description = "Наименование группы")
    String name,
    // AUDIT-FIX: тип группы в кратком ответе
    @Schema(description = "Тип группы пользователей")
    UserGroupType groupType
) {
    public static UserGroupShortResponseDTO from(UserGroup userGroup) {
        return new UserGroupShortResponseDTO(
                userGroup.getId(),
                userGroup.getName(),
                userGroup.getGroupType()
        );
    }
}
