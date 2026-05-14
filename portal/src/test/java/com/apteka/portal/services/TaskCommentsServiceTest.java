package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.apteka.portal.dtos.response.TaskCommentResponseDTO;
import com.apteka.portal.exceptions.AvtorCommentNotInputException;
import com.apteka.portal.exceptions.TaskCommentNotFoundException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskComment;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.repository.AptekaRepository;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskCommentRepository;
import com.apteka.portal.repository.TaskRepository;

@ExtendWith(MockitoExtension.class)
public class TaskCommentsServiceTest {

	@Mock
	private AptekaRepository aptekaRepository;

	@Mock
	private TaskCommentRepository taskCommentsRepository;

	@Mock
	private TaskRepository taskRepository;

	@Mock
	private ClientRepository clientRepository;

	@InjectMocks
	private TaskCommentService taskCommentsService;

	@Test
	void getAll_Success() {
		when(taskCommentsRepository.findAll())
				.thenReturn(List.of(TaskComment.builder().build()));

		List<TaskCommentResponseDTO> result = taskCommentsService.getAll();

		assertEquals(1, result.size());
		verify(taskCommentsRepository, times(1)).findAll();
	}

	@Test
	void getByTask_WhenTaskExists_Success() {
		Long taskId = 1L;

		when(taskRepository.existsById(taskId)).thenReturn(true);
		when(taskCommentsRepository.findByTaskId(taskId))
				.thenReturn(List.of(TaskComment.builder().build()));

		List<TaskCommentResponseDTO> result = taskCommentsService.getByTask(taskId);

		assertEquals(1, result.size());

		verify(taskRepository, times(1)).existsById(taskId);
		verify(taskCommentsRepository, times(1)).findByTaskId(taskId);
	}

	@Test
	void getByTask_WhenTaskNotFound_Throws() {
		Long taskId = 99L;

		when(taskRepository.existsById(taskId)).thenReturn(false);

		assertThrows(TaskNotFoundException.class,
				() -> taskCommentsService.getByTask(taskId));

		verify(taskRepository, times(1)).existsById(taskId);
		verifyNoInteractions(taskCommentsRepository);
	}

	@Test
	void getOne_WhenExists_Success() {
		Long id = 1L;
		TaskComment comment = TaskComment.builder().build();

		when(taskCommentsRepository.findById(id))
				.thenReturn(Optional.of(comment));

		TaskCommentResponseDTO result = taskCommentsService.getOne(id);

		assertNotNull(result);
		verify(taskCommentsRepository, times(1)).findById(id);
	}

	@Test
	void getOne_WhenNotFound_Throws() {
		Long id = 1L;

		when(taskCommentsRepository.findById(id))
				.thenReturn(Optional.empty());

		assertThrows(TaskCommentNotFoundException.class,
				() -> taskCommentsService.getOne(id));

		verify(taskCommentsRepository, times(1)).findById(id);
	}

	@Test
	void create_WhenClientAuthor_Success() {
		Long taskId = 1L;
		String commentText = "Комментарий";

		Task taskProxy = Task.builder().id(taskId).build();
		AppUserDetails user = TestData.mockJustUser();

		Client client = Client.builder()
				.id(user.getClientId())
				.fullName("Иван Иванов")
				.build();

		TaskComment saved = TaskComment.builder()
				.id(10L)
				.comment(commentText)
				.task(taskProxy)
				.client(client)
				.build();

		// Исправлено под оптимизацию сервиса: existsById и getReferenceById
		when(taskRepository.existsById(taskId)).thenReturn(true);
		when(taskRepository.getReferenceById(taskId)).thenReturn(taskProxy);
		when(clientRepository.getReferenceById(user.getClientId())).thenReturn(client);
		when(taskCommentsRepository.save(any(TaskComment.class))).thenReturn(saved);

		TaskCommentResponseDTO result = taskCommentsService.create(commentText, taskId, user);

		assertNotNull(result);
		assertEquals(commentText, result.comment());
		assertEquals(user.getClientId(), result.authorId());
		assertEquals("CLIENT", result.authorType().name());

		verify(taskRepository, times(1)).existsById(taskId);
		verify(taskRepository, times(1)).getReferenceById(taskId);
		verify(clientRepository, times(1)).getReferenceById(user.getClientId());
		verify(taskCommentsRepository, times(1)).save(any(TaskComment.class));
	}

	@Test
	void create_WhenAptekaAuthor_Success() {
		Long taskId = 1L;
		String commentText = "Комментарий";

		Task taskProxy = Task.builder().id(taskId).build();
		AppUserDetails user = TestData.mockJustApteka();
		UserGroup userGroup = UserGroup.builder().id(1).name("Центральная").build();

		Apteka apteka = Apteka.builder()
				.id(user.getAptekaId())
				.number(5)
				.userGroup(userGroup) // Настраиваем группу, чтобы избежать NPE в DTO
				.build();

		TaskComment saved = TaskComment.builder()
				.id(20L)
				.comment(commentText)
				.task(taskProxy)
				.apteka(apteka)
				.build();

		// Исправлено под оптимизацию сервиса: existsById и getReferenceById
		when(taskRepository.existsById(taskId)).thenReturn(true);
		when(taskRepository.getReferenceById(taskId)).thenReturn(taskProxy);
		when(aptekaRepository.getReferenceById(user.getAptekaId())).thenReturn(apteka);
		when(taskCommentsRepository.save(any(TaskComment.class))).thenReturn(saved);

		TaskCommentResponseDTO result = taskCommentsService.create(commentText, taskId, user);

		assertNotNull(result);
		assertEquals(commentText, result.comment());
		assertEquals(user.getAptekaId(), result.authorId());
		assertEquals("APTEKA", result.authorType().name());

		verify(taskRepository, times(1)).existsById(taskId);
		verify(taskRepository, times(1)).getReferenceById(taskId);
		verify(aptekaRepository, times(1)).getReferenceById(user.getAptekaId());
		verify(taskCommentsRepository, times(1)).save(any(TaskComment.class));
	}

	@Test
	void create_WhenTaskNotFound_Throws() {
		Long taskId = 999L;
		AppUserDetails user = TestData.mockJustUser();

		// Исправлено на existsById
		when(taskRepository.existsById(taskId)).thenReturn(false);

		assertThrows(TaskNotFoundException.class,
				() -> taskCommentsService.create("text", taskId, user));

		verify(taskRepository, times(1)).existsById(taskId);
		verifyNoInteractions(taskCommentsRepository);
		verifyNoInteractions(clientRepository);
	}

	@Test
	void create_WhenUserHasNoType_Throws() {
		Long taskId = 1L;
		Task taskProxy = Task.builder().id(taskId).build();

		AppUserDetails user = mock(AppUserDetails.class);
		when(user.isClient()).thenReturn(false);
		when(user.isApteka()).thenReturn(false);

		// Исправлено на existsById и getReferenceById
		when(taskRepository.existsById(taskId)).thenReturn(true);
		when(taskRepository.getReferenceById(taskId)).thenReturn(taskProxy);

		assertThrows(AvtorCommentNotInputException.class,
				() -> taskCommentsService.create("text", taskId, user));

		verify(taskRepository, times(1)).existsById(taskId);
		verify(taskRepository, times(1)).getReferenceById(taskId);
	}
}