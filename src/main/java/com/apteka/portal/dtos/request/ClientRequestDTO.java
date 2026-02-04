package com.apteka.portal.dtos.request;

public record ClientRequestDTO (
    String login,
    String password,
    String fullName,
    String role,
    Integer groupClientId
)
{}
