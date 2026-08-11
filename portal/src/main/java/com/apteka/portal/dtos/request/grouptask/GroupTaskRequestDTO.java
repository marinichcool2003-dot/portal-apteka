package com.apteka.portal.dtos.request.grouptask;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на создание группы задач")
public record GroupTaskRequestDTO(
        @Schema(description = "Наименование")
        @NotBlank(message = "Наименование группы задач не может быть пустым!")
        String name,
        @Schema(description = "Идентификатор группы-создателя")
        @NotNull(message = "Идентификатор группы создателя не может быть пустым!")
        @Positive(message = "Группа создателя должна быть больше нуля!")
        Integer creatorGroupId,
        @Schema(description = "Идентификатор группы-исполнителя")
        @NotNull(message = "Идентификатор группы создателя не может быть пустым")
        @Positive(message = "Группа создателя должна быть больше нуля!")
        Integer intendedGroupId
)
{}
