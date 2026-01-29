package com.apteka.portal.dtos.request;

import java.util.UUID;

public record TaskRequestCreatedByAptekaToClientDTO(
    String title,
    String description,
    String comments,
    Integer createByAptekaId,
    Integer workTypeId,
    UUID assignedClientId
) 
{}
