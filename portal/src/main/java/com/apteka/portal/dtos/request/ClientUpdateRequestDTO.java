package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

public record ClientUpdateRequestDTO(
    String login,
    String password,
    MultipartFile avatar
) 
{}
