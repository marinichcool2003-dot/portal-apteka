package com.apteka.portal.dtos.request;

import java.util.Set;

public record ClientRequestDTO (
    String login,
    String password,
    String fullName,
    Set<String> roles,
    Integer groupClientId
)
{}
