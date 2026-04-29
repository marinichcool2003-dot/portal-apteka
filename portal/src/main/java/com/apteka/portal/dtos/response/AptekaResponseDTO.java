package com.apteka.portal.dtos.response;

import com.apteka.portal.models.Apteka;

public record AptekaResponseDTO(
    Integer id,
    String login,
    String adress,
    String phoneNumber,
    Integer groupId,
    String groupName
) 
{
    public static AptekaResponseDTO from(Apteka apteka){
        return new AptekaResponseDTO(
            apteka.getAptekaId(),
            apteka.getLogin(), 
            apteka.getAdress(),
            apteka.getPhoneNumber(),
            apteka.getUserGroup() != null ? apteka.getUserGroup().getId() : null,
            apteka.getUserGroup() != null ? apteka.getUserGroup().getName() : null
        );
    }
}
