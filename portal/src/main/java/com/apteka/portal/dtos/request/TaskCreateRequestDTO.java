package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskCreateRequestDTO(
    String title,
    String description,
    String comments,
    Integer workTypeId,
    Integer createdByAptekaId,
    UUID createdByClientId,
    Integer assignedAptekaId,
    UUID assignedClientId,
    Integer assignedGroupId
) {}
