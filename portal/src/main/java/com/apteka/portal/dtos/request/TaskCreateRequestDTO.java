package com.apteka.portal.dtos.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record TaskCreateRequestDTO(
    @NotBlank
    String title,

    @NotBlank
    String description,

    @NotBlank
    Integer workTypeId,

    String statusDescription,

    Integer assignedAptekaId,
    UUID assignedClientId
) {}
