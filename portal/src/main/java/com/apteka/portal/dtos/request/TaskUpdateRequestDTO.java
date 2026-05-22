package com.apteka.portal.dtos.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskUpdateRequestDTO(
        @NotBlank(message = "Заголовок задачи не может быть пустым") String title,

        @NotBlank(message = "Описание задачи не может быть пустым") String description,

        @Positive(message = "Идентификатор вида работ должен быть положительным числом") Integer workTypeId,

        @NotBlank(message = "Статус задачи не может быть пустым") String statusDescription,

        @NotBlank(message = "Статус задачи не может быть пустым") String priorityDescription,

        Integer assignedAptekaId,
        UUID assignedClientId
    ) implements TaskRequestDTO {}
