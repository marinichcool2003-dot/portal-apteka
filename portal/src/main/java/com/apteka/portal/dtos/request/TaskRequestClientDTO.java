package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskRequestClientDTO(
    String title,
    String description,
    String comments,
    UUID createdByClient,
    Integer groupId
) 
{}

