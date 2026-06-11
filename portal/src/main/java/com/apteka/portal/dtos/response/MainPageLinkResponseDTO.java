package com.apteka.portal.dtos.response;

import com.apteka.portal.models.MainPageLink;

public record MainPageLinkResponseDTO(
        Integer id,
        String name,
        String link,
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
