package com.apteka.portal.dtos.request;

public record AptekaFilterRequestDTO(
    String login,
    Integer groupId,
    Integer number,
    String phoneNumber
) 
{}
