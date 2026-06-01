package com.apteka.portal.dtos.request;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskUpdateRequestDTO(
        @Pattern(regexp = "^(?!\\s*$).+", message = "Заголовок задачи не может быть пустым, но может быть null") String title,

        @Pattern(regexp = "^(?!\\s*$).+", message = "Описание задачи не может быть пустым, но может быть null") String description,

        @Positive(message = "Идентификатор вида работ должен быть положительным числом") Integer workTypeId,

        @Schema(description = "Статус задачи (Открыта, В процессе, Закрыта, Отклонена)", example = "Открыта", allowableValues = {
                "Открыта", "В процессе", "Закрыта",
                "Отклонена" }) @Pattern(regexp = "^(?!\\s*$).+", message = "Статус задачи не может быть пустым, но может быть null") String statusDescription,

        @Schema(description = "Приоритет задачи (Низкий, Средний, Высокий)", example = "Низкий", allowableValues = {
                "Низкий", "Средний",
                "Высокий" }) @Pattern(regexp = "^(?!\\s*$).+", message = "Приоритет задачи не может быть пустым, но может быть null") String priorityDescription,

        @Positive(message = "Идентификатор аптеки должен быть больше нуля") Integer assignedAptekaId,
        UUID assignedClientId) implements TaskRequestDTO {
}
