package com.apteka.portal.dtos.request.client;

public record ClientFilterRequestDTO (
    String login,
    String phoneNumber,
    Integer groupId,
    String fullName,
    String extensionNumber,
    Boolean isActive
){}
