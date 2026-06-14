package com.apteka.portal.dtos.response;

import java.util.Set;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.UserType;

public record AppUserDetailsResponseDTO(
    Object id,
    String login,
    String displayName,
    Set<UserRole> roles,
    UserGroupResponseDTO userGroupResponseDTO,
    UserType type
) {
    public static AppUserDetailsResponseDTO from(AppUserDetails appUserDetails) {
        return new AppUserDetailsResponseDTO(
            appUserDetails.getInternalId(),
            appUserDetails.getLogin(),
            appUserDetails.getDisplayName(), 
            appUserDetails.getRoles(), 
            new UserGroupResponseDTO(
                appUserDetails.getUserGroup().getId(),
                appUserDetails.getUserGroup().getName(), 
                appUserDetails.getUserGroup().getPhoneNumber()
            ), 
            appUserDetails.getType());
    }
}
