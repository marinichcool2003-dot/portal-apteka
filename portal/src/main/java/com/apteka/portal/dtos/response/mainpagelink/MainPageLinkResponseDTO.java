package com.apteka.portal.dtos.response.mainpagelink;

import com.apteka.portal.models.MainPageLink;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными ссылки главной страницы")
public record MainPageLinkResponseDTO(
        @Schema(description = "Идентификатор")
        Integer id,
        @Schema(description = "Наименование")
        String name,
        @Schema(description = "URL-ссылка")
        String link,
        @Schema(description = "Группа ссылок главной страницы")
        GroupMainPageLinksResponseDTO groupMainPageLinksResponseDTO) {
    public static MainPageLinkResponseDTO from(MainPageLink mainPageLink) {
        return new MainPageLinkResponseDTO(
                mainPageLink.getId(),
                mainPageLink.getName(),
                mainPageLink.getLink(),
                new GroupMainPageLinksResponseDTO(
                        mainPageLink.getGroupMainPageLinks().getId(),
                        mainPageLink.getGroupMainPageLinks().getName(),
                        mainPageLink.getGroupMainPageLinks().getDescription()));
    }
}
