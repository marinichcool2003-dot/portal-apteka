package com.apteka.portal.dtos.request;

import java.util.UUID;

import lombok.Builder;

@Builder
public record TaskUpdateRequestDTO(
        String title,

        String description,

        Integer workTypeId,

        String statusDescription,

        String priorityDescription,

        Integer assignedAptekaId,
        UUID assignedClientId
    ) implements TaskRequestDTO {}
