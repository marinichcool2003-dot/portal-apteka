package com.apteka.portal.dtos.response;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserRole;

public record AptekaResponseDTO(
    UUID id,
    String login,
    String adress,
    String phoneNumber,
    Set<UserRole> roles,
    UserGroupShortResponseDTO userGroup
)
{
    public static AptekaResponseDTO from(Apteka apteka){
        return new AptekaResponseDTO(
            apteka.getId(),
            apteka.getAccount().getLogin(), 
            apteka.getAdress(),
            apteka.getPhoneNumber(),
            apteka.getRoles(),
            Optional.ofNullable(apteka.getAccount().getUserGroup())
                .map(group -> new UserGroupShortResponseDTO(group.getId(), group.getName()))
                .orElse(null)
        );
    }
}
