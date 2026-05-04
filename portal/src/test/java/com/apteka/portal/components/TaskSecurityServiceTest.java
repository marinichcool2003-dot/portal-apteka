package com.apteka.portal.components;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.services.ClientService;
import com.apteka.portal.services.TestData;

@ExtendWith(MockitoExtension.class)
public class TaskSecurityServiceTest {
    @Mock
    private ClientService clientService;

    private TaskRequestDTO createDto(String title,
            String description,
            String comments,
            String workTypeId,
            String statusDescription,
            UUID assignedClientId,
            Integer assignedAptekaId,
            Integer assignedGroupId) {
        return new TaskRequestDTO(title,
                description,
                comments,
                assignedGroupId,
                statusDescription,
                assignedAptekaId,
                assignedClientId,
                assignedGroupId);
    }

    @Test
    public void validateCanCreate_WhenAccess() {
        
    }
}
