package com.apteka.portal.dtos.response.usergroup;

import java.util.List;

public record UserGroupWithVisibleResponseDTO(
        Integer id,
        String name,
        String phoneNumber,
        String internalNumber,
        String extensionNumber,
        String avatarUrl,
        Boolean isActive,
        List<UserGroupShortResponseDTO> visibleGroups
) {}
