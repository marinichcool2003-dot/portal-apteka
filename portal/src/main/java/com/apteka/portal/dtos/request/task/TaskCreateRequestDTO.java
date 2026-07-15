package com.apteka.portal.dtos.request.task;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskCreateRequestDTO(
        @NotBlank(message = "Заголовок задачи не может быть пустым") String title,

        @NotBlank(message = "Описание задачи не может быть пустым") String description,

        @NotNull(message = "Идентификатор вида работ не может быть пустым") @Positive(message = "Идентификатор вида работ не может быть меньше нуля") Integer workTypeId,

        UUID assignerId) implements TaskRequestDTO {
}
