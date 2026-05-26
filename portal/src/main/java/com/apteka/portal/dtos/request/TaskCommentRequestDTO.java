package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskCommentRequestDTO(
    @NotBlank(message = "Комментарий не может быть пустым")
    String commentText,

    @NotNull
    @Positive(message = "Идентификатор задачи должен быть больше нуля")
    Long taskId
) {}
