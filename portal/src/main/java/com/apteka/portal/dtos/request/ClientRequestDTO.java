package com.apteka.portal.dtos.request;

import com.apteka.portal.models.GroupClient;

public record ClientRequestDTO (
    String login,
    String password,
    String fullName,
    String role,
    GroupClient groupClient
)
{}
