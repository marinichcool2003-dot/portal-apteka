package com.apteka.portal.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.exceptions.BlockChangeIfNotActuallyTaskException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;
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
        void validateCanCreate_WhenWorkTypeNotForAssignedUser() {
                AppUserDetails currentUser = TestData.mockJustBoss();
                WorkType workType = TestData.defaultWorkType();
                UserGroup userGroup = TestData.defaulUserGroup();
                Client client = Client.builder().id(UUID.randomUUID()).userGroup(userGroup).build();

                TaskRequestDTO dto = TaskRequestDTO.builder()
                                .assignedClientId(client.getId())
                                .workTypeId(workType.getId())
                                .build();

                when(clientService.getOne(client.getId())).thenReturn(client);

                AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                                () -> taskSecurityService.validateCanCreate(dto, currentUser));

                assertEquals("Вы можете ставить задачи сотруднику в рамке вида работ его группы",
                                exception.getMessage());

                verify(clientService, times(1)).getOne(client.getId());
        }

        @Test
        void validateCanCreate_WhenHasElevatedPrivileges() {
                AppUserDetails currentUser = TestData.mockJustSenior();
                WorkType workType = TestData.defaultWorkType();
                UserGroup userGroup = TestData.defaulUserGroup();
                Client client = Client.builder().id(UUID.randomUUID()).userGroup(userGroup).build();

                TaskRequestDTO dto = TaskRequestDTO.builder()
                                .assignedClientId(client.getId())
                                .workTypeId(workType.getId())
                                .build();

                when(clientService.getOne(client.getId())).thenReturn(client);
                when(workTypeService.getOne(workType.getId())).thenReturn(workType);

                assertDoesNotThrow(() -> taskSecurityService.validateCanCreate(dto, currentUser));

                verify(clientService, times(1)).getOne(client.getId());
                verify(workTypeService, times(1)).getOne(workType.getId());
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
                                () -> taskSecurityService.validateCanUpdate(task, dto, currentUser));

                assertEquals("Аптека не может изменять описание задачи, которую уже создала", exception.getMessage());
        }

        @Test
        void changeAssigner_WhenUserHasElevatedPrivileges() {
                AppUserDetails currentUser = TestData.mockJustSenior();
                WorkType workType = TestData.newDefaultWorkType();
                Integer workTypeId = workType.getId();

                when(workTypeService.getOne(workTypeId)).thenReturn(workType);

                Task task = Task.builder().assignedClient(null).workType(workType).build();
                TaskRequestDTO dto = TaskRequestDTO.builder().assignedClientId(UUID.randomUUID()).workTypeId(workTypeId)
                                .build();

                boolean result = assertDoesNotThrow(() -> taskSecurityService.changeAssigner(task, dto, currentUser));

                assertTrue(result, "Метод должен вернуть true");
        }

        @Test
        void changeAssigner_WhenUsersInGroupAndUserRelatedToTask() {
                AppUserDetails currentUser = TestData.mockJustUser();
                UserGroup userGroup = TestData.defaulUserGroup();
                Client oldAssignedClient = Client.builder().id(currentUser.getClientId()).userGroup(userGroup).build();
                Client newAssignedClient = Client.builder().id(UUID.randomUUID()).userGroup(userGroup).build();
                WorkType workType = TestData.defaultWorkType();
                Task task = Task.builder().assignedClient(oldAssignedClient).workType(workType).build();
                TaskRequestDTO dto = TaskRequestDTO.builder().assignedClientId(newAssignedClient.getId())
                                .workTypeId(workType.getId()).build();

                when(workTypeService.getOne(workType.getId())).thenReturn(workType);
                when(clientService.getOne(newAssignedClient.getId())).thenReturn(newAssignedClient);

                boolean result = assertDoesNotThrow(() -> taskSecurityService.changeAssigner(task, dto, currentUser));
                assertTrue(result, "Метод должен вернуть true");

                verify(workTypeService, times(1)).getOne(workType.getId());
                verify(clientService, times(1)).getOne(newAssignedClient.getId());
        }

        @Test
        void changeAssigner_WhenAssignerUserInAnotherGroup() {
                AppUserDetails currentUser = TestData.mockJustUser();
                UserGroup myGroup = currentUser.getUserGroup();
                UserGroup anotherGroup = TestData.newDefaulUserGroup();
                Client oldAssignedClient = Client.builder().id(currentUser.getClientId()).userGroup(myGroup).build();
                Client newAssignedClient = Client.builder().id(UUID.randomUUID()).userGroup(anotherGroup).build();
                WorkType workType = TestData.defaultWorkType();

                workType.getGroupTask().setUserGroup(myGroup);

                Task task = Task.builder()
                                .assignedClient(oldAssignedClient)
                                .workType(workType)
                                .build();

                TaskRequestDTO dto = TaskRequestDTO.builder()
                                .assignedClientId(newAssignedClient.getId())
                                .workTypeId(workType.getId())
                                .build();

                when(workTypeService.getOne(dto.workTypeId())).thenReturn(workType);
                when(clientService.getOne(dto.assignedClientId())).thenReturn(newAssignedClient);

                AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                                () -> taskSecurityService.changeAssigner(task, dto, currentUser));

                assertEquals(
                                "Пользователь без прав доступа может изменять исполнителей только своих собственных или назначенных ему задач и переводить их внутри своей группы",
                                exception.getMessage());

                verify(workTypeService, times(1)).getOne(dto.workTypeId());
                verify(clientService, times(1)).getOne(dto.assignedClientId());
        }

        @Test
        void validateStatus_WhenAptekaChangeStatus_Succesful() {
                AppUserDetails currentUser = TestData.mockJustApteka();
                Apteka apteka = Apteka.builder().id(currentUser.getAptekaId()).build();
                Task task = Task.builder().createdByApteka(apteka).build();
                task.changeStatus(TaskStatus.OPEN);

                assertDoesNotThrow(() -> {
                        taskSecurityService.validateStatus(task, currentUser);
                });
        }

        @Test
        void validateStatus_WhenSeniorChangeStatus_Succesful() {
                AppUserDetails currentUser = TestData.mockJustSenior();
                Task task = Task.builder().title("Заголовок").build();
                task.changeStatus(TaskStatus.OPEN);

                assertDoesNotThrow(() -> {
                        taskSecurityService.validateStatus(task, currentUser);
                });
        }

        @Test
        void validateStatus_WhenSeniorChangeStatusAlreadyClosedTask() {
                AppUserDetails currentUser = TestData.mockJustSenior();
                Task task = Task.builder().title("Заголовок").build();
                task.changeStatus(TaskStatus.CLOSED);

                LocalDateTime closeDate = LocalDateTime.of(2023, 1, 1, 12, 0);
                task.changeStatus(TaskStatus.CLOSED);
                task.setClosingDate(closeDate);

                LocalDateTime marchDate = LocalDateTime.of(2023, 3, 1, 12, 0);

                try (MockedStatic<LocalDateTime> mockedTime = mockStatic(LocalDateTime.class)) {
                        mockedTime.when(LocalDateTime::now).thenReturn(marchDate);

                        assertThrows(BlockChangeIfNotActuallyTaskException.class, () -> {
                                taskSecurityService.validateStatus(task, currentUser);
                        });
                }
        }
}
