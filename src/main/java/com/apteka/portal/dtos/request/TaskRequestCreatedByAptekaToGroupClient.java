package com.apteka.portal.dtos.request;

public record TaskRequestCreatedByAptekaToGroupClient(
    String title,
    String description,
    String comments,
    Integer createdAptekaId,
    Integer assignedGroupClientId,
    Integer workTypeId
) {}
