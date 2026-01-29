package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskRequestCreatedByClientToClientDTO(
    String title,
    String description,
    String comments,
    UUID creatorClient,
    UUID assignedClient,
    Integer workTypeId
) 
{}

