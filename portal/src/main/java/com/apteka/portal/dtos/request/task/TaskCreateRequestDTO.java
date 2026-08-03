package com.apteka.portal.dtos.request.task;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@Schema(description = "Запрос на создание задачи")
public record TaskCreateRequestDTO(
        @Schema(description = "Заголовок")
        @NotBlank(message = "Заголовок задачи не может быть пустым") String title,
        @Schema(description = "Описание")
        @NotBlank(message = "Описание задачи не может быть пустым") String description,
        @Schema(description = "Идентификатор типа работ")
        @NotNull(message = "Идентификатор вида работ не может быть пустым") @Positive(message = "Идентификатор вида работ не может быть меньше нуля") Integer workTypeId,
        @Schema(description = "Идентификатор исполнителя задачи")
        UUID assignerId) implements TaskRequestDTO {
}
