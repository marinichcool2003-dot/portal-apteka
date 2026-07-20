package com.apteka.portal.dtos.response.apteka;

import java.util.UUID;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Address;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;

public record AptekaResponseDTO(
    UUID id,
    String login,
    AdressResponseDTO adress,
    UserRole role,
    UserGroupShortResponseDTO userGroup,
    Integer number,
    String phoneNumber,
    boolean isEnabled
)
{
    public static AptekaResponseDTO from(Apteka apteka){
        Account account = apteka.getAccount();
        Address address = apteka.getAddress();
        if (account == null) {
            return new AptekaResponseDTO(
                apteka.getId(),
                null,
                address != null ? new AdressResponseDTO(address.getCity(), address.getStreet(), address.getHouse(), address.getFiasId()) : null, 
                null, 
                null, 
                apteka.getNumber(), 
                null, false);
        }

        UserGroup group = account.getUserGroup();

        boolean isAccountActive = Boolean.TRUE.equals(account.isActive());

        boolean isGroupActive = (group == null) || Boolean.TRUE.equals(group.isActive());

        boolean isEnabled = isAccountActive && isGroupActive;

        return new AptekaResponseDTO(
            apteka.getId(),
            account.getLogin(),
            address != null ? new AdressResponseDTO(address.getCity(), address.getStreet(), address.getHouse(), address.getFiasId()) : null,
            apteka.getRole(),
            group != null ? new UserGroupShortResponseDTO(group.getId(), group.getName()) : null,
            apteka.getNumber(),
            account.getPhoneNumber(),
            isEnabled
        );
    }
}
