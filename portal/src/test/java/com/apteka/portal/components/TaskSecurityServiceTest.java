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
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
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

    @Test
    void validateCanCreate_UserToGroup() {
        Integer workTypeId = TestData.defaultWorkType().getId();

        TaskRequestDTO dto = TaskRequestDTO.builder()
                .workTypeId(workTypeId)
                .build();

        AppUserDetails currentUser = TestData.mockJustUser();

        when(workTypeService.getOne(workTypeId)).thenReturn(TestData.defaultWorkType());

        assertDoesNotThrow(() -> {
            taskSecurityService.validateCanCreate(dto, currentUser);
        });

        verify(workTypeService, times(1)).getOne(workTypeId);
    }

    @Test
    void validateCanCreate_UserToAnotherUserInGroup() {
        Integer workTypeId = TestData.newDefaultWorkType().getId();
        UUID assignedClientId = TestData.mockJustSenior().getClientId();
        TaskRequestDTO dto = TaskRequestDTO.builder()
                .workTypeId(workTypeId)
                .assignedClientId(assignedClientId)
                .build();
        AppUserDetails currentUser = TestData.mockJustUser();

        when(workTypeService.getOne(workTypeId)).thenReturn(TestData.newDefaultWorkType());

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskSecurityService.validateCanCreate(dto, currentUser));

        assertEquals("Вы можете ставить задачи только сотрудникам своей группы или аптекам",
                exception.getMessage());

        verify(workTypeService, times(1)).getOne(workTypeId);
    }

    @Test
    void validateCanCreate_WhenCurrentUserIsApteka() {
        AppUserDetails currentUser = TestData.mockJustApteka();

        Integer workTypeId = TestData.newDefaultWorkType().getId();
        TaskRequestDTO dto = TaskRequestDTO.builder()
                .workTypeId(workTypeId)
                .assignedClientId(UUID.randomUUID())
                .build();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskSecurityService.validateCanCreate(dto, currentUser));

        assertEquals("Аптека не может ставить задачи на конкретного сотрудника", exception.getMessage());
    }

    @Test
    void calidateCanCreate_WhenHasElevatedPrivileges() {
        AppUserDetails currentUser = TestData.mockJustSenior();

        TaskRequestDTO dto = TaskRequestDTO.builder()
                .assignedClientId(UUID.randomUUID())
                .build();

        assertDoesNotThrow(() -> {
            taskSecurityService.validateCanCreate(dto, currentUser);
        });
    }

    @Test
    void validateCanUpdate_WhenUserUpdateHisTask() {
        AppUserDetails currentUser = TestData.mockJustUser();
        Client assignedClient = Client.builder()
                .id(currentUser.getClientId())
                .build();
        Task task = Task.builder()
                .description("Описание")
                .assignedClient(assignedClient)
                .build();
        TaskRequestDTO dto = TaskRequestDTO.builder()
                .description("Новое Описание")
                .build();

        assertDoesNotThrow(() -> {
            taskSecurityService.validateCanUpdate(task, dto, currentUser);
        });

    }

    @Test
    void validateCanUpdate_WhenUserUpdateNotHisTask() {
        AppUserDetails currentUser = TestData.mockJustUser();
        Client assignedClient = Client.builder()
                .id(UUID.randomUUID())
                .build();
        Task task = Task.builder()
                .description("Описание")
                .assignedClient(assignedClient)
                .build();
        TaskRequestDTO dto = TaskRequestDTO.builder()
                .description("Новое Описание")
                .build();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> taskSecurityService.validateCanUpdate(task, dto, currentUser));

        assertEquals(
                "Обычный пользователь не может изменять задачу вне своего отдела или к которой не имеет отношение!",
                exception.getMessage());
    }

    @Test 
    void validateCanUpdate_WhenUserIsApteka() {
        AppUserDetails currentUser = TestData.mockJustApteka();
        Task task = Task.builder()
            .description("Описание")
            .build();
        TaskRequestDTO dto = TaskRequestDTO.builder()
            .description("Новое Описание")
            .build();
   
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> taskSecurityService.validateCanUpdate(task, dto, currentUser)
        );

        assertEquals("Аптека не может изменять описание задачи, которую уже создала", exception.getMessage());
    }

    @Test
    void changeAssigner_WhenUserHasElevatedPrivileges() {
        AppUserDetails currentUser = TestData.mockJustSenior();
        Client oldAssignedClient = null;
        Client newAssignedClient = Client.builder().id(UUID.randomUUID()).build();
        Task task = Task.builder().assignedClient(oldAssignedClient).build();
        TaskRequestDTO dto = TaskRequestDTO.builder().assignedClientId(newAssignedClient.getId()).build();

        assertDoesNotThrow(() -> taskSecurityService.changeAssigner(task, dto, currentUser));
    }
}
