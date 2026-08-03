package com.apteka.portal.dtos.request.task;

import java.util.UUID;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import io.swagger.v3.oas.annotations.media.Schema;

@Builder
@Schema(description = "Запрос на обновление задачи")
public record TaskUpdateRequestDTO(
        @Schema(description = "Заголовок")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Заголовок задачи не может быть пустым, но может быть null") String title,
        @Schema(description = "Описание")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Описание задачи не может быть пустым, но может быть null") String description,
        @Schema(description = "Идентификатор типа работ")
        @Positive(message = "Идентификатор вида работ должен быть положительным числом") Integer workTypeId,
        @Schema(description = "Код статуса задачи")
        @Pattern(regexp = "^(?!\\s*$).+", message = "Статус задачи не может быть пустым, но может быть null") String statusCode,
        @Schema(description = "Идентификатор исполнителя задачи")
        UUID assignerId) implements TaskRequestDTO {
}
