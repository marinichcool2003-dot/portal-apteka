package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record TaskCommentRequestDTO(
    @NotBlank
    String commentText,

    @NotNull
    @Positive
    Long taskId
) {}
