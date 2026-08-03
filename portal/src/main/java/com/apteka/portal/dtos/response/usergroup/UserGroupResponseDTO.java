package com.apteka.portal.dtos.response.usergroup;

import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserGroupType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Полный ответ по группе пользователей")
public record UserGroupResponseDTO(
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
    // AUDIT-FIX: тип группы в ответе
    @Schema(description = "Тип группы пользователей")
    UserGroupType groupType
) {
    public static UserGroupResponseDTO from(UserGroup userGroup) {
        return new UserGroupResponseDTO(
            userGroup.getId(),
            userGroup.getName(),
            userGroup.getPhoneNumber(),
            userGroup.getInternalNumber(),
            userGroup.getExtensionNumber(),
            userGroup.getAvatarUrl(),
            userGroup.isActive(),
            userGroup.getGroupType());
    }
}
