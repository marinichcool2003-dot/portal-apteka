package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskRequestDTO(
    String title,
    String description,
    String comments,
    Integer workTypeId,
    Integer assignedAptekaId,
    UUID assignedClientId,
    Integer assignedGroupId
) {}
