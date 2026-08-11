package com.apteka.portal.dtos.request.usergroup;

import java.util.Set;

import com.apteka.portal.models.UserGroupType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Запрос на создание группы пользователей")
public record UserGroupRequestDTO(
        @Schema(description = "Наименование группы", example = "Социальные аптеки")
        @NotBlank(message = "Наименование группы не может быть пустым")
        String name,

        @Schema(description = "Номер телефона группы", example = "+79001234567")
        @Pattern(
                regexp = "^\\+?[0-9]{7,15}$",
                message = "Номер телефона должен быть в формате +123456789"
        )
        String phoneNumber,

        @Schema(description = "Внутренний номер", example = "101")
        @Pattern(regexp = "^[0-9]{1,20}$")
        String internalNumber,

        @Schema(description = "Добавочный номер", example = "10")
        @Pattern(regexp = "^[0-9]{1,20}$")
        String extensionNumber,

        @Schema(description = "Идентификаторы видимых групп")
        Set<Integer> visibleGroups,

        @Schema(description = "Тип группы пользователей", example = "APTEKA_GROUP")
        @NotNull(message = "Тип группы не может быть пустым")
        UserGroupType groupType
) implements UserGroupRequestInterface {}
