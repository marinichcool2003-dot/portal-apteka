package com.apteka.portal.dtos.request;

import com.apteka.portal.models.GroupClient;

public record ClientUpdateDTO(
    String login,
    String password,
    String role,
    GroupClient groupClient
) {}
