package com.apteka.portal.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record TaskCommentRequestDTO(
    @NotBlank
    String commentText,

    @NotNull
    Long taskId
) {}
