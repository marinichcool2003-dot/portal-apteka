package com.apteka.portal.dtos.response.apteka;

import java.util.UUID;

import com.apteka.portal.dtos.response.usergroup.UserGroupShortResponseDTO;
import com.apteka.portal.models.Account;
import com.apteka.portal.models.Address;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Ответ с данными аптеки")
public record AptekaResponseDTO(
    @Schema(description = "Идентификатор")
    UUID id,
    @Schema(description = "Наименование аптеки")
    String aptekaName,
    @Schema(description = "Логин пользователя")
    String login,
    @Schema(description = "Email")
    String email,
    @Schema(description = "Адрес")
    AdressResponseDTO adress,
    @Schema(description = "Роль пользователя")
    UserRole role,
    @Schema(description = "Группа пользователей")
    UserGroupShortResponseDTO userGroup,
    @Schema(description = "Номер аптеки")
    Integer number,
    @Schema(description = "Номер телефона")
    String phoneNumber,
    @Schema(description = "Признак доступности учётной записи")
    boolean isEnabled
)
{
    public static AptekaResponseDTO from(Apteka apteka){
        Account account = apteka.getAccount();
        Address address = apteka.getAddress();
        if (account == null) {
            return new AptekaResponseDTO(
                apteka.getId(),
                apteka.getAptekaName(),
                null,
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
            apteka.getAptekaName(),
            account.getLogin(),
            account.getEmail(),
            address != null ? new AdressResponseDTO(address.getCity(), address.getStreet(), address.getHouse(), address.getFiasId()) : null,
            apteka.getRole(),
            group != null ? UserGroupShortResponseDTO.from(group) : null,
            apteka.getNumber(),
            account.getPhoneNumber(),
            isEnabled
        );
    }
}
