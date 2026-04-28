package com.apteka.portal.dtos.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;

public record TaskRequestDTO(
    @NotBlank
    String title,
    @NotBlank
    String description,
    String comments,
    Integer workTypeId,
    String statusDescription,
    Integer assignedAptekaId,
    UUID assignedClientId,
    Integer assignedGroupId
) {}
