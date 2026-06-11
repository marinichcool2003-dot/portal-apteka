package com.apteka.portal.dtos.response;

import com.apteka.portal.models.GroupMainPageLinks;

public record GroupMainPageLinksResponseDTO(
    Integer id,
    String name,
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
