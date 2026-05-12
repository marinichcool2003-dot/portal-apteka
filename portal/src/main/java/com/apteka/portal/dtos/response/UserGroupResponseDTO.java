package com.apteka.portal.dtos.response;

import com.apteka.portal.models.UserGroup;

public record UserGroupResponseDTO(
    Integer id,
    String name,
    String phoneNumber
) {
    public static UserGroupResponseDTO from(UserGroup userGroup) {
        return new UserGroupResponseDTO(
            userGroup.getId(),
            userGroup.getName(),
            userGroup.getPhoneNumber());
    }
}
