package com.apteka.portal.dtos.response.rating;

import java.time.Instant;
import java.util.UUID;

import com.apteka.portal.models.AptekaTaskRating;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Оценка аптеки по задаче")
public record AptekaTaskRatingResponseDTO(
        @Schema(description = "Идентификатор оценки")
        Long id,
        @Schema(description = "Идентификатор задачи")
        Long taskId,
        @Schema(description = "Идентификатор аптеки")
        UUID aptekaId,
        @Schema(description = "Идентификатор оценившего сотрудника")
        UUID raterAccountId,
        @Schema(description = "Имя оценившего сотрудника")
        String raterName,
        @Schema(description = "Оценка от 1 до 5 звёзд")
        Integer stars,
        @Schema(description = "Причина / комментарий к оценке")
        String reason,
        @Schema(description = "Количество правок сотрудником")
        Integer employeeEditCount,
        @Schema(description = "Дата создания")
        Instant createdAt,
        @Schema(description = "Дата последнего обновления")
        Instant updatedAt
) {
    public static AptekaTaskRatingResponseDTO from(AptekaTaskRating rating) {
        String raterName = null;
        if (rating.getRaterAccount() != null && rating.getRaterAccount().getClient() != null) {
            raterName = rating.getRaterAccount().getClient().getFullName();
        }

        return new AptekaTaskRatingResponseDTO(
                rating.getId(),
                rating.getTask().getId(),
                rating.getApteka().getId(),
                rating.getRaterAccount().getId(),
                raterName,
                rating.getStars(),
                rating.getReason(),
                rating.getEmployeeEditCount(),
                rating.getCreatedAt(),
                rating.getUpdatedAt());
    }
}
