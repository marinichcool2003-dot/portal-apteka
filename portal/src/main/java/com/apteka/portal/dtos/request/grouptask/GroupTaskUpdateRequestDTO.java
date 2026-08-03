package com.apteka.portal.dtos.request.grouptask;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Запрос на обновление группы задач")
public record GroupTaskUpdateRequestDTO(
        @Schema(description = "Наименование")
        @NotEmpty(message = "Наименование группы задач не может быть пустым!") String name,
        @Schema(description = "Идентификатор группы-создателя")
        @Positive(message = "Группа создателя должна быть больше нуля!") Integer creatorGroupId,
        @Schema(description = "Идентификатор группы-исполнителя")
        @Positive(message = "Группа создателя должна быть больше нуля!") Integer intendedGroupId) {
}
