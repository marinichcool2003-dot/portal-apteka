package com.apteka.portal.dtos.response.usergroup;

import com.apteka.portal.models.UserGroup;

public record UserGroupShortResponseDTO(
    Integer id,
    String name
) {
    public static UserGroupShortResponseDTO from(UserGroup userGroup) {
        return new UserGroupShortResponseDTO(
                userGroup.getId(),
                userGroup.getName()
        );
    }
}
