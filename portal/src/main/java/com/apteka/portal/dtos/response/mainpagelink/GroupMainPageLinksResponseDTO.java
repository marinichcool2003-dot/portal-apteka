package com.apteka.portal.dtos.response.mainpagelink;

import com.apteka.portal.models.GroupMainPageLinks;
import io.swagger.v3.oas.annotations.media.Schema;

// AUDIT-FIX: @Schema Swagger RU
@Schema(description = "Ответ с данными группы ссылок главной страницы")
public record GroupMainPageLinksResponseDTO(
    @Schema(description = "Идентификатор")
    Integer id,
    @Schema(description = "Наименование")
    String name,
    @Schema(description = "Описание")
    String description
) {
    public static GroupMainPageLinksResponseDTO from(GroupMainPageLinks groupMainPageLinks) {
        return new GroupMainPageLinksResponseDTO(
            groupMainPageLinks.getId(), 
            groupMainPageLinks.getName(), 
            groupMainPageLinks.getDescription()
        );
    }
}
