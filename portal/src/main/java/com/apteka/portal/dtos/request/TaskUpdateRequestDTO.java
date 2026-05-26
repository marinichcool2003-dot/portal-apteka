package com.apteka.portal.dtos.request;

import java.util.UUID;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskUpdateRequestDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "Заголовок задачи не может быть пустым, но может быть null") String title,

        @Pattern(regexp = "^(?!\\s*$).+", message = "Описание задачи не может быть пустым, но может быть null") String description,

        @Positive(message = "Идентификатор вида работ должен быть положительным числом") Integer workTypeId,

        @Pattern(regexp = "^(?!\\s*$).+", message = "Статус задачи не может быть пустым, но может быть null") String statusDescription,

        @Pattern(regexp = "^(?!\\s*$).+", message = "Приоритет задачи не может быть пустым, но может быть null") String priorityDescription,

        Integer assignedAptekaId,
        UUID assignedClientId
    ) implements TaskRequestDTO {}
