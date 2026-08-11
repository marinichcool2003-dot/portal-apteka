package com.apteka.portal.dtos.request.client;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;
import io.swagger.v3.oas.annotations.media.Schema;


@Schema(description = "Запрос на обновление личного профиля клиента")
public record ClientUpdatePersonalProfileRequestDTO(
        @Schema(description = "Данные обновления аккаунта")
        AccountUpdateRequestDTO accountUpdateRequestDTO,
        @Schema(description = "Данные обновления профиля клиента")
        ClientUpdateDescriptionRequestDTO clientUpdateRequestDTO,
        @Schema(description = "Файл аватара")
        MultipartFile avatar
) {}
