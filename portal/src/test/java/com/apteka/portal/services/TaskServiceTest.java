package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apteka.portal.components.TaskAuditService;
import com.apteka.portal.components.TaskSecurityService;
import com.apteka.portal.dtos.request.TaskCreateRequestDTO;
import com.apteka.portal.dtos.request.TaskUpdateRequestDTO;
import com.apteka.portal.dtos.response.TaskShortResponseDTO;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.WorkType;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;
import com.apteka.portal.repository.WorkTypeRepository;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private AptekaRepository aptekaRepository;

	@Mock
	private ClientRepository clientRepository;

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

		TaskCreateRequestDTO dto = TaskCreateRequestDTO.builder()
				.title("   Не работает   касса  ")
				.description("Н   ужно проверить кас  су до  конца дня")
				.workTypeId(workType.getId())
				.statusDescription(null)
				.assignedAptekaId(null)
				.assignedClientId(null)
				.build();

		Task savedTask = Task.builder()
				.id(10L)
				.title("Не работает касса")
				.description("Нужно проверить кассу до конца дня")
				.workType(workType)
				.createdByApteka(creator)
				.assignedClient(null)
				.build();

		when(workTypeRepository.getReferenceById(workType.getId()))
				.thenReturn(workType);

		when(aptekaRepository.findById(currentUser.getAptekaId()))
				.thenReturn(Optional.of(creator));

		when(taskRepository.save(any(Task.class)))
				.thenReturn(savedTask);

		TaskShortResponseDTO result = taskService.create(dto, currentUser);

		assertNotNull(result);

		assertEquals(savedTask.getTitle(), result.title());
		assertEquals(savedTask.getDescription(), result.description());

		verify(taskSecurityService, times(1))
				.validateCanCreate(dto, currentUser);

		verify(workTypeRepository, times(1))
				.getReferenceById(workType.getId());

		verify(aptekaRepository, times(1))
				.findById(currentUser.getAptekaId());

		verify(taskRepository, times(1))
				.save(any(Task.class));

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
				.workType(oldWorkType)
				.createdByApteka(creator)
				.assignedClient(oldAssigner)
				.build();

		TaskUpdateRequestDTO dto = TaskUpdateRequestDTO.builder()
				.title("Не работает терминал")
				.description("Нужно проверить терминал срочно")
				.workTypeId(newWorkType.getId())
				.assignedClientId(newAssigner.getId())
				.build();

		Task updatedTask = Task.builder()
				.id(10L)
				.title("Не работает терминал")
				.description("Нужно проверить терминал срочно")
				.workType(newWorkType)
				.createdByApteka(creator)
				.assignedClient(newAssigner)
				.build();

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

		when(clientRepository.existsById(newAssigner.getId()))
				.thenReturn(true);

		when(taskRepository.save(any(Task.class)))
				.thenReturn(updatedTask);

		TaskShortResponseDTO result = taskService.update(taskForUpdate.getId(), dto, currentUser);

		assertNotNull(result);

		assertEquals(updatedTask.getTitle(), result.title());
		assertEquals(updatedTask.getDescription(), result.description());

		assertEquals(
				updatedTask.getWorkType().getName(),
				result.workTypeName());

		assertEquals(
				updatedTask.getAssignedClient().getId(),
				result.assignedBy().id());

		verify(taskRepository, times(1))
				.findById(taskForUpdate.getId());

		verify(taskSecurityService, times(1))
				.validateCanUpdate(taskForUpdate, dto, currentUser);

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

		verify(clientRepository, times(1))
				.existsById(newAssigner.getId());

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