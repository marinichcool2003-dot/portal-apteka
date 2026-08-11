package com.apteka.portal.dtos.request.client;

import org.springframework.web.multipart.MultipartFile;

import com.apteka.portal.dtos.request.AccountUpdateRequestDTO;

import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Полный запрос на обновление клиента")
public record ClientUpdateFullRequestDTO(
        @Schema(description = "Данные обновления аккаунта")
        AccountUpdateRequestDTO accountUpdateRequestDTO,
        @Schema(description = "Данные обновления профиля клиента")
        ClientUpdateDescriptionRequestDTO clientUpdateRequestDTO,
        @Schema(description = "Файл аватара")
        MultipartFile avatar,
        @Schema(description = "Идентификатор группы пользователей")
        @Positive(message = "Идентификатор группы должен быть больше нуля!") Integer userGroupId
) {}
