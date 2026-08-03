package com.apteka.portal.dtos.request.usergroup;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.models.UserGroupType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на обновление группы пользователей")
public record UserGroupUpdateRequestDTO(
        @Schema(description = "Наименование группы")
        String name,

        @Schema(description = "Номер телефона группы")
        @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Номер телефона должен быть в формате +123456789")
        String phoneNumber,

        @Schema(description = "Внутренний номер")
        @Pattern(regexp = "^[0-9]{1,20}$")
        String internalNumber,

        @Schema(description = "Добавочный номер")
        @Pattern(regexp = "^[0-9]{1,20}$")
        String extensionNumber,

        @Schema(description = "Идентификаторы видимых групп для добавления")
        Set<Integer> visibleGroups,

        @Schema(description = "Аватар группы")
        MultipartFile avatar,

        @Schema(description = "Тип группы пользователей")
        UserGroupType groupType
) implements UserGroupRequestInterface {
}
