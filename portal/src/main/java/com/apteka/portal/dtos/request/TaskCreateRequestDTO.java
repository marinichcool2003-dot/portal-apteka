package com.apteka.portal.dtos.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskCreateRequestDTO(
        @NotBlank(message = "Заголовок задачи не может быть пустым") String title,

        @NotBlank(message = "Описание задачи не может быть пустым") String description,

        @NotNull(message = "Идентификатор вида работ не может быть пустым") @Positive(message = "Идентификатор вида работ не может быть меньше нуля") Integer workTypeId,

        @Schema(description = "Статус задачи (Открыта, В процессе, Закрыта, Отклонена)", example = "OPEN", allowableValues = {"Открыта", "В процессе", "Закрыта", "Отклонена"})
        @NotBlank(message = "Статус задачи не может быть пустым") String statusDescription,

        @Positive Integer assignedAptekaId,
        UUID assignedClientId) implements TaskRequestDTO {
}
