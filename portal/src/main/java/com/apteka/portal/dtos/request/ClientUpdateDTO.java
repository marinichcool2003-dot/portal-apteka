package com.apteka.portal.dtos.request;

public record ClientUpdateDTO(
    String login,
    String password,
    Integer groupClientId
) {}
