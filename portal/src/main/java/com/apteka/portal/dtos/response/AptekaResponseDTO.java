package com.apteka.portal.dtos.response;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.apteka.portal.models.Account;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserRole;

public record AptekaResponseDTO(
    UUID id,
    String login,
    String adress,
    Set<UserRole> roles,
    UserGroupShortResponseDTO userGroup,
    Integer number,
    String phoneNumber,
    boolean isActive
)
{
    public static AptekaResponseDTO from(Apteka apteka){
        return new AptekaResponseDTO(
            apteka.getId(),
            apteka.getAccount().getLogin(), 
            apteka.getAdress(),
            apteka.getRoles(),
            Optional.ofNullable(apteka.getAccount().getUserGroup())
                .map(group -> new UserGroupShortResponseDTO(group.getId(), group.getName()))
                .orElse(null),
            apteka.getNumber(),
            Optional.ofNullable(apteka.getAccount()).map(Account::getPhoneNumber).orElse(null),
            apteka.getAccount().isActive()
        );
    }
}
