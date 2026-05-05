package com.apteka.portal.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.services.ClientService;
import com.apteka.portal.services.TestData;
import com.apteka.portal.services.WorkTypeService;

@ExtendWith(MockitoExtension.class)
public class TaskSecurityServiceTest {
    @Mock
    private ClientService clientService;
    @Mock
    private WorkTypeService workTypeService;

    @InjectMocks
    private TaskSecurityService taskSecurityService;

    private TaskRequestDTO createDto(
            String title,
            String description,
            String comments,
            Integer workTypeId,
            String statusDescription,
            Integer assignedAptekaId,
            UUID assignedClientId) {
        return new TaskRequestDTO(
                title,
                description,
                comments,
                workTypeId,
                statusDescription,
                assignedAptekaId,
                assignedClientId);
    }

    @Test
    public void validateCanCreate_UserToGroup() {
        Integer workTypeId = TestData.defaultWorkType().getId();
        TaskRequestDTO dto = createDto("Заголовок", 
            "Описание", "Сомментарии", 
            TestData.defaultWorkType().getId(), null,
            null, null);
        AppUserDetails currentUser = TestData.mockJustUser();

        when(workTypeService.getOne(workTypeId)).thenReturn(TestData.defaultWorkType());

        assertDoesNotThrow(() -> {
            taskSecurityService.validateCanCreate(dto, currentUser);
        });

        verify(workTypeService, times(1)).getOne(workTypeId);
    }

    @Test
    public void validateCanCreate_UserToAnotherUserInGroup() {
        Integer workTypeId = TestData.newDefaultWorkType().getId();
        TaskRequestDTO dto = createDto("Заголовок", 
            "Описание", "Сомментарии", 
            TestData.defaultWorkType().getId(), null,
            null, null);
        AppUserDetails currentUser = TestData.mockJustUser();

        when(workTypeService.getOne(workTypeId)).thenReturn(TestData.newDefaultWorkType());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> taskSecurityService.validateCanCreate(dto, currentUser));

        assertEquals("Вы можете ставить задачи только сотрудникам своей группы или аптекам", 
            exception.getMessage());
        
        verify(workTypeService, times(1)).getOne(workTypeId);
    }
}
