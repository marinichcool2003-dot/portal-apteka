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
            apteka.getId(),
            apteka.getLogin(), 
            apteka.getAdress(),
            apteka.getPhoneNumber(),
            apteka.getGroup() != null ? apteka.getGroup().getId() : null,
            apteka.getGroup() != null ? apteka.getGroup().getName() : null
        );
    }
}
