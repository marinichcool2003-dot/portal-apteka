package com.apteka.portal.dtos.request;

import org.springframework.web.multipart.MultipartFile;

public record ClientAvatarRequestDTO(
    MultipartFile avatar
) 
{}
