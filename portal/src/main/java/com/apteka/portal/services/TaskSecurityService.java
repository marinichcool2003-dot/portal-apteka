package com.apteka.portal.services;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.ClientRole;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TaskSecurityService {
    public void validateCanCreate(TaskCreateRequestDTO dto, Object currentUser) {
        if (currentUser instanceof Client client) {
            if (client.getRole() == ClientRole.ADMIN || client.getRole() == ClientRole.BOSS || client.getRole() == ClientRole.SENIOR) return;
            if (client.getRole() == ClientRole.USER) {
                if (!client.getUserGroup().getId().equals(dto.assignedGroupId())) {
                    throw new AccessDeniedException("Вы можете ставить задачи только сотрудникам своей группы");
                }
            }
        }
        if (currentUser instanceof Apteka) {

            if (dto.assignedGroupId() != null && dto.assignedClientId() != null) {
                throw new AccessDeniedException("Аптека не может ставить задачи на конкретного сотрудника");
            }
        }
    }
}
