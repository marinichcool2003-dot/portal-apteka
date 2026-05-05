package com.apteka.portal.dtos.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TaskRequestDTO(
    @NotBlank
    String title,

    @NotBlank
    String description,

    String comments,

    @NotBlank
    Integer workTypeId,

    @NotBlank
    String statusDescription,

    Integer assignedAptekaId,
    UUID assignedClientId
) {}
