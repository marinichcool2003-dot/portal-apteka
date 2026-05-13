package com.apteka.portal.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
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
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.WorkTypeRepository;
import com.apteka.portal.services.TestData;

@ExtendWith(MockitoExtension.class)
public class TaskSecurityServiceTest {
	@Mock
	private ClientRepository clientRepository;
	@Mock
	private WorkTypeRepository workTypeRepository;

	@InjectMocks
	private TaskSecurityService taskSecurityService;

	@Test
	void validateCanCreate_UserToGroup() {
		Integer workTypeId = TestData.defaultWorkType().getId();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.workTypeId(workTypeId)
				.build();

		AppUserDetails currentUser = TestData.mockJustUser();

		when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(TestData.defaultWorkType()));

		assertDoesNotThrow(() -> {
			taskSecurityService.validateCanCreate(dto, currentUser);
		});

		verify(workTypeRepository, times(1)).findById(workTypeId);
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

		when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(TestData.newDefaultWorkType()));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> taskSecurityService.validateCanCreate(dto, currentUser));

		assertEquals("Вы можете ставить задачи только сотрудникам своей группы или аптекам",
				exception.getMessage());

		verify(workTypeRepository, times(1)).findById(workTypeId);
	}

	@Test
	void validateCanCreate_WhenCurrentUserIsApteka() {
		AppUserDetails currentUser = TestData.mockJustApteka();

		Integer workTypeId = 2;
		WorkType workType = new WorkType().builder().id(workTypeId).build();
		TaskRequestDTO dto = TaskRequestDTO.builder()
				.workTypeId(workTypeId)
				.assignedClientId(UUID.randomUUID())
				.build();
		when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(workType));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> taskSecurityService.validateCanCreate(dto, currentUser));

		assertEquals("Аптека не может ставить задачи на конкретного сотрудника", exception.getMessage());
		verify(workTypeRepository, times(1)).findById(workTypeId);
	}

	@Test
	void validateCanCreate_WhenWorkTypeNotForAssignedUser() {
		AppUserDetails currentUser = TestData.mockJustBoss();
		Integer anotherWorkTypeId = 222;
		WorkType anotherWorkType = WorkType.builder().id(anotherWorkTypeId).build();
		UserGroup userGroup = TestData.defaulUserGroup();
		Client client = Client.builder().id(UUID.randomUUID()).userGroup(userGroup).build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.assignedClientId(client.getId())
				.workTypeId(anotherWorkTypeId)
				.build();

		when(workTypeRepository.findById(anotherWorkTypeId)).thenReturn(Optional.of(anotherWorkType));

		when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> taskSecurityService.validateCanCreate(dto, currentUser));

		assertEquals("Вы можете ставить задачи сотруднику в рамке вида работ его группы",
				exception.getMessage());

		verify(clientRepository, times(1)).findById(client.getId());
		verify(workTypeRepository, times(1)).findById(anotherWorkTypeId);
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

		when(clientRepository.findById(client.getId())).thenReturn(Optional.of(client));
		when(workTypeRepository.findById(workType.getId())).thenReturn(Optional.of(workType));

		assertDoesNotThrow(() -> taskSecurityService.validateCanCreate(dto, currentUser));

		verify(clientRepository, times(1)).findById(client.getId());
		verify(workTypeRepository, times(1)).findById(workType.getId());
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

		when(workTypeRepository.findById(workTypeId)).thenReturn(Optional.of(workType));

		Task task = Task.builder().assignedClient(null).workType(workType).build();
		TaskRequestDTO dto = TaskRequestDTO.builder().assignedClientId(UUID.randomUUID()).workTypeId(workTypeId)
				.build();

		boolean result = assertDoesNotThrow(() -> taskSecurityService.changeAssigner(task, dto, currentUser));

		assertTrue(result, "Метод должен вернуть true");

		verify(workTypeRepository, times(1)).findById(workTypeId);
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

		when(workTypeRepository.findById(workType.getId())).thenReturn(Optional.of(workType));
		when(clientRepository.findById(newAssignedClient.getId())).thenReturn(Optional.of(newAssignedClient));

		boolean result = assertDoesNotThrow(() -> taskSecurityService.changeAssigner(task, dto, currentUser));
		assertTrue(result, "Метод должен вернуть true");

		verify(workTypeRepository, times(1)).findById(workType.getId());
		verify(clientRepository, times(1)).findById(newAssignedClient.getId());
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

		when(workTypeRepository.findById(workType.getId())).thenReturn(Optional.of(workType));
		when(clientRepository.findById(dto.assignedClientId())).thenReturn(Optional.of(newAssignedClient));

		AccessDeniedException exception = assertThrows(AccessDeniedException.class,
				() -> taskSecurityService.changeAssigner(task, dto, currentUser));

		assertEquals(
				"Пользователь без прав доступа может изменять исполнителей только своих собственных или назначенных ему задач и переводить их внутри своей группы",
				exception.getMessage());

		verify(workTypeRepository, times(1)).findById(dto.workTypeId());
		verify(clientRepository, times(1)).findById(dto.assignedClientId());
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

	@Test
	void changeAssigner_WhenAssignmentNotChanged_ReturnFalse() {
		AppUserDetails currentUser = TestData.mockJustUser();

		Client assignedClient = Client.builder()
				.id(currentUser.getClientId())
				.userGroup(TestData.defaulUserGroup())
				.build();

		WorkType workType = TestData.defaultWorkType();

		Task task = Task.builder()
				.assignedClient(assignedClient)
				.workType(workType)
				.build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.assignedClientId(assignedClient.getId())
				.workTypeId(workType.getId())
				.build();

		when(workTypeRepository.findById(workType.getId()))
				.thenReturn(Optional.of(workType));

		boolean result = taskSecurityService.changeAssigner(task, dto, currentUser);

		assertTrue(!result);

		verify(workTypeRepository, times(1))
				.findById(workType.getId());
	}

	@Test
	void changeWorkTypeToAnotherDepartament_WhenSenior_ReturnTrue() {
		AppUserDetails currentUser = TestData.mockJustSenior();

		WorkType oldWorkType = TestData.defaultWorkType();
		WorkType newWorkType = TestData.newDefaultWorkType();
		newWorkType.setId(2);

		Task task = Task.builder()
				.workType(oldWorkType)
				.build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.workTypeId(newWorkType.getId())
				.build();

		boolean result = taskSecurityService
				.changeWorkTypeToAnotherDepartament(task, dto, currentUser);

		assertTrue(result);
	}

	@Test
	void changeWorkTypeToAnotherDepartament_WhenJustUser_ReturnFalse() {
		AppUserDetails currentUser = TestData.mockJustUser();

		WorkType oldWorkType = TestData.defaultWorkType();
		WorkType newWorkType = TestData.newDefaultWorkType();

		Task task = Task.builder()
				.workType(oldWorkType)
				.build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.workTypeId(newWorkType.getId())
				.build();

		boolean result = taskSecurityService
				.changeWorkTypeToAnotherDepartament(task, dto, currentUser);

		assertTrue(!result);
	}

	@Test
	void validateStatus_WhenUserRelatedToTask_Success() {
		AppUserDetails currentUser = TestData.mockJustUser();

		Client assignedClient = Client.builder()
				.id(currentUser.getClientId())
				.build();

		Task task = Task.builder()
				.assignedClient(assignedClient)
				.build();

		task.changeStatus(TaskStatus.OPEN);

		assertDoesNotThrow(() -> {
			taskSecurityService.validateStatus(task, currentUser);
		});
	}

	@Test
	void validateStatus_WhenUserNotRelated_Throws() {
		AppUserDetails currentUser = TestData.mockJustUser();

		Client assignedClient = Client.builder()
				.id(UUID.randomUUID())
				.build();

		Task task = Task.builder()
				.assignedClient(assignedClient)
				.build();

		task.changeStatus(TaskStatus.OPEN);

		AccessDeniedException exception = assertThrows(
				AccessDeniedException.class,
				() -> taskSecurityService.validateStatus(task, currentUser));

		assertEquals(
				"Пользователь без прав доступа может изменять только свои собственные или назначенные ему задачи",
				exception.getMessage());
	}

	@Test
	void validateStatus_WhenAptekaNotRelated_Throws() {
		AppUserDetails currentUser = TestData.mockJustApteka();

		Apteka anotherApteka = Apteka.builder()
				.id(999)
				.build();

		Task task = Task.builder()
				.createdByApteka(anotherApteka)
				.build();

		task.changeStatus(TaskStatus.OPEN);

		AccessDeniedException exception = assertThrows(
				AccessDeniedException.class,
				() -> taskSecurityService.validateStatus(task, currentUser));

		assertEquals(
				"Аптека может изменять только свои собственные или назначенные ей задачи",
				exception.getMessage());
	}

	@Test
	void validateStatus_WhenTaskDenied_Throws() {
		AppUserDetails currentUser = TestData.mockJustSenior();

		Task task = Task.builder()
				.title("Task")
				.build();

		task.changeStatus(TaskStatus.DENIED);

		assertThrows(
				BlockChangeIfNotActuallyTaskException.class,
				() -> taskSecurityService.validateStatus(task, currentUser));
	}

	@Test
	void validateStatus_WhenTaskClosedLessThanMonth_Success() {
		AppUserDetails currentUser = TestData.mockJustSenior();

		Task task = Task.builder()
				.title("Task")
				.build();

		task.changeStatus(TaskStatus.CLOSED);

		LocalDateTime closeDate = LocalDateTime.now().minusDays(10);
		task.setClosingDate(closeDate);

		assertDoesNotThrow(() -> {
			taskSecurityService.validateStatus(task, currentUser);
		});
	}

	@Test
	void validateCanCreate_WhenUserAssignTaskToAptekaFromAnotherGroup_Success() {
		AppUserDetails currentUser = TestData.mockJustUser();

		WorkType workType = TestData.newDefaultWorkType();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.workTypeId(workType.getId())
				.assignedAptekaId(10)
				.build();

		when(workTypeRepository.findById(workType.getId()))
				.thenReturn(Optional.of(workType));

		assertDoesNotThrow(() -> {
			taskSecurityService.validateCanCreate(dto, currentUser);
		});

		verify(workTypeRepository, times(1))
				.findById(workType.getId());
	}

	@Test
	void validateCanUpdate_WhenDtoIsEmpty_Success() {
		AppUserDetails currentUser = TestData.mockJustUser();

		Task task = Task.builder()
				.title("Title")
				.description("Description")
				.build();

		TaskRequestDTO dto = TaskRequestDTO.builder().build();

		assertDoesNotThrow(() -> {
			taskSecurityService.validateCanUpdate(task, dto, currentUser);
		});

		verifyNoInteractions(clientRepository);
		verifyNoInteractions(workTypeRepository);
	}
}
