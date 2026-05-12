package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apteka.portal.components.TaskSecurityService;
import com.apteka.portal.dtos.request.TaskRequestDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private AptekaService aptekaService;

	@Mock
	private ClientService clientService;

	@Mock
	private WorkTypeRepository workTypeRepository;

	@Mock
	private TaskAuditService taskAuditService;

	@Mock
	private TaskSecurityService taskSecurityService;

	@InjectMocks
	private TaskService taskService;

	@Test
	void create_Succesful() {

		AppUserDetails currentUser = TestData.mockJustApteka();

		UserGroup userGroup = UserGroup.builder()
				.id(1)
				.name("САК")
				.build();

		GroupTask groupTask = GroupTask.builder()
				.id(1)
				.userGroup(userGroup)
				.build();

		WorkType workType = WorkType.builder()
				.id(1)
				.name("Проверка кассы")
				.groupTask(groupTask)
				.build();

		Apteka creator = Apteka.builder().id(currentUser.getAptekaId()).build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.title("   Не работает   касса  ")
				.description("Н   ужно проверить кас  су до  конца дня")
				.comments(null)
				.workTypeId(workType.getId())
				.statusDescription(null)
				.assignedAptekaId(null)
				.assignedClientId(null)
				.build();

		Task savedTask = Task.builder()
				.id(10L)
				.title("Не работает касса")
				.description("Нужно проверить кассу до конца дня")
				.comments(null)
				.workType(workType)
				.createdByApteka(creator)
				.assignedClient(null)
				.build();

		try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {

			mockedStatic.when(SecurityUtils::getRequiredCurrentUser)
					.thenReturn(currentUser);

			when(workTypeRepository.getReferenceById(workType.getId()))
					.thenReturn(workType);

			when(aptekaService.getOne(currentUser.getAptekaId()))
					.thenReturn(creator);

			when(taskRepository.save(any(Task.class)))
					.thenReturn(savedTask);

			Task result = taskService.create(dto);

			assertNotNull(result);

			assertEquals(savedTask.getTitle(), result.getTitle());
			assertEquals(savedTask.getDescription(), result.getDescription());
			assertEquals(savedTask.getComments(), result.getComments());

			verify(taskSecurityService, times(1))
					.validateCanCreate(dto, currentUser);

			verify(workTypeRepository, times(1))
					.getReferenceById(workType.getId());

			verify(aptekaService, times(1))
					.getOne(currentUser.getAptekaId());

			verify(taskRepository, times(1))
					.save(any(Task.class));
		}
	}

	@Test
	void update_Succesful() {

		AppUserDetails currentUser = TestData.mockJustSenior();

		WorkType oldWorkType = TestData.defaultWorkType();
		WorkType newWorkType = TestData.newDefaultWorkType();

		Client oldAssigner = Client.builder()
				.id(UUID.randomUUID())
				.fullName("Иванов Иван")
				.userGroup(TestData.defaulUserGroup())
				.build();

		Client newAssigner = Client.builder()
				.id(UUID.randomUUID())
				.fullName("Петров Петр")
				.userGroup(TestData.newDefaulUserGroup())
				.build();

		Apteka creator = Apteka.builder()
				.id(10)
				.build();

		Task taskForUpdate = Task.builder()
				.id(10L)
				.title("Не работает касса")
				.description("Нужно проверить кассу до конца дня")
				.comments(null)
				.workType(oldWorkType)
				.createdByApteka(creator)
				.assignedClient(oldAssigner)
				.build();

		TaskRequestDTO dto = TaskRequestDTO.builder()
				.title("Не работает терминал")
				.description("Нужно проверить терминал срочно")
				.comments("Очень срочно")
				.workTypeId(newWorkType.getId())
				.assignedClientId(newAssigner.getId())
				.assignedAptekaId(null)
				.statusDescription(null)
				.build();

		Task updatedTask = Task.builder()
				.id(10L)
				.title("Не работает терминал")
				.description("Нужно проверить терминал срочно")
				.comments("Очень срочно")
				.workType(newWorkType)
				.createdByApteka(creator)
				.assignedClient(newAssigner)
				.build();

		try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {

			mockedStatic.when(SecurityUtils::getRequiredCurrentUser)
					.thenReturn(currentUser);

			when(taskRepository.findById(taskForUpdate.getId()))
					.thenReturn(Optional.of(taskForUpdate));

			when(taskSecurityService.changeWorkTypeToAnotherDepartament(
					taskForUpdate,
					dto,
					currentUser))
					.thenReturn(true);

			when(taskSecurityService.changeAssigner(
					taskForUpdate,
					dto,
					currentUser))
					.thenReturn(true);

			when(workTypeRepository.getReferenceById(newWorkType.getId()))
					.thenReturn(newWorkType);

			when(clientService.getOne(newAssigner.getId()))
					.thenReturn(newAssigner);

			when(taskRepository.save(any(Task.class)))
					.thenReturn(updatedTask);

			Task result = taskService.update(taskForUpdate.getId(), dto);

			assertNotNull(result);

			assertEquals(updatedTask.getTitle(), result.getTitle());
			assertEquals(updatedTask.getDescription(), result.getDescription());
			assertEquals(updatedTask.getComments(), result.getComments());

			assertEquals(
					updatedTask.getWorkType().getName(),
					result.getWorkType().getName());

			assertEquals(
					updatedTask.getAssignedClient().getId(),
					result.getAssignedClient().getId());

			verify(taskRepository, times(1))
					.findById(taskForUpdate.getId());

			verify(taskSecurityService, times(1))
					.validateCanUpdate(taskForUpdate, dto, currentUser);

			verify(taskSecurityService, times(1))
					.validateStatus(taskForUpdate, currentUser);

			verify(taskSecurityService, times(1))
					.changeWorkTypeToAnotherDepartament(
							taskForUpdate,
							dto,
							currentUser);

			verify(taskSecurityService, times(1))
					.changeAssigner(
							taskForUpdate,
							dto,
							currentUser);

			verify(workTypeRepository, times(1))
					.getReferenceById(newWorkType.getId());

			verify(clientService, times(1))
					.getOne(newAssigner.getId());

			verify(taskAuditService, times(3))
					.logChange(
							any(),
							any(),
							anyString(),
							any(),
							any());

			verify(taskRepository, times(1))
					.save(any(Task.class));
		}
	}
}