package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskUpdateRequestDTO(
        String title,

        String description,

        Integer workTypeId,

        String statusDescription,

        String priorityDescription,

        Integer assignedAptekaId,
        UUID assignedClientId
    ) {
}
