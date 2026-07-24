package com.apteka.portal.dtos.request.client;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;


public record ClientUpdatePersonalProfileRequestDTO(
    AccountUpdateRequestDTO accountUpdateRequestDTO,
    ClientUpdateDescriptionRequestDTO clientUpdateRequestDTO,
    MultipartFile avatar
) {}
