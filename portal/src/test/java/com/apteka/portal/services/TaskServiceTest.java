package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidTaskDescriptionException;
import com.apteka.portal.exceptions.InvalidTaskTitleException;
import com.apteka.portal.exceptions.TaskNotFoundException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.Task;
import com.apteka.portal.models.TaskStatus;
import com.apteka.portal.repository.ClientRepository;
import com.apteka.portal.repository.TaskRepository;

public class TaskServiceTest {
    @Mock
    private AptekaService aptekaService;

    @Mock
    private TaskRepository taskInterface;

    @Mock
    private GroupTaskService groupTaskService;

    @Mock
    private ClientRepository clientInterface;

    @InjectMocks
    private TaskService taskService;

    private Apteka mockApteka;
    private GroupTask mockGroupTask;
    private Client mockClient;
    private GroupApteki mockGroupApteki;
    private UserGroup mockgroupClient;
    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        mockGroupApteki = new GroupApteki(1, "Группа1");
        mockgroupClient = new UserGroup(1, "Группа1");
        mockApteka = new Apteka(1, "sacapteka101@farmp.ru", "15943245", 001, "Ростов-на-Дону", "9881233322", mockGroupApteki);
        mockGroupTask = new GroupTask(1, "Группа 1");
        UUID newUUID = UUID.randomUUID();
        mockClient = new Client(newUUID, "Birdux", "1234_Af!848", "Гетманцев Даниил Олегович", UserRole.LEGEND, mockgroupClient, "url/avatar");
        task1 = new Task(
                1L,
                "Заголовок1",
                "Описание",
                "Комментарий",
                new Date(),
                TaskStatus.OPEN,
                mockApteka,
                mockGroupTask);
        task2 = new Task(
                2L,
                "Заголовок2",
                "Описание",
                "Комментарий",
                new Date(),
                TaskStatus.OPEN,
                mockApteka,
                mockGroupTask);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll_ShouldReturnList() {

        List<Task> mockTasks = List.of(task1, task2);

        when(taskInterface.findAll()).thenReturn(mockTasks);

        List<Task> result = taskService.getAll().join();

        assertEquals(2, result.size());
        assertEquals("Заголовок2", result.get(1).getTitle());
        verify(taskInterface, times(1)).findAll();
    }

    @Test
    void testGetOne_ShouldReturnTask_WhenExists() {
        when(taskInterface.findById(1L)).thenReturn(Optional.of(task1));

        Task result = taskService.getOne(1L).join();

        assertEquals("Заголовок1", result.getTitle());
        verify(taskInterface).findById(1L);
    }

    @Test
    void testGetByApteka_WhenExists_ShouldReturnList() {

        when(aptekaService.getOne(1)).thenReturn(mockApteka);

        List<Task> mockTasks = List.of(task1, task2);
        when(taskInterface.findByAptekaId(mockApteka.getId())).thenReturn(mockTasks);

        List<Task> result = taskService.getByApteka(mockApteka.getId()).join();

        assertEquals(2, result.size());
        assertEquals("Заголовок1", result.get(0).getTitle());
        assertEquals("Заголовок2", result.get(1).getTitle());
        verify(aptekaService).getOne(1);
        verify(taskInterface).findByAptekaId(mockApteka.getId());
    }

    @Test
    void testGetByApteka_WhenAptekaNotFound_ShouldThrow() {
        when(aptekaService.getOne(1)).thenThrow(AptekaNotFoundException.class);

        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.getByApteka(1).join());

        assertTrue(thrown.getCause() instanceof AptekaNotFoundException);
        verify(aptekaService).getOne(1);
        verifyNoInteractions(taskInterface);
    }

    @SuppressWarnings("null")
    @Test
    void testCreate_ShouldReturnTask_WhenValid() {
        String title = "Заголовок1";
        String description = "Описание";
        String comments = "Комментарий";

        when(aptekaService.getOne(mockApteka.getId())).thenReturn(mockApteka);
        when(groupTaskService.getOne(mockGroupTask.getId())).thenReturn(mockGroupTask);

        Task saved = Task.builder()
                .title(title)
                .description(description)
                .comments(comments)
                .apteka(mockApteka)
                .group(mockGroupTask)
                .build();
        when(taskInterface.save(any(Task.class))).thenReturn(saved);

        Task result = taskService.create(
                title,
                description,
                comments,
                mockApteka.getId(),
                mockGroupTask.getId())
                .join();

        assertEquals("Заголовок1", result.getTitle());
        assertEquals(mockApteka, result.getApteka());
        assertEquals(mockGroupTask, result.wor());

        verify(aptekaService).getOne(mockApteka.getId());
        verify(groupTaskService).getOne(mockGroupTask.getId());
        verify(taskInterface).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void create_WhenTitleIsNull_ShouldThrowInvalidTaskTitleException() {
        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.create(null, "Описание", "Комментарий", 1, 1).join());

        assertTrue(thrown.getCause() instanceof InvalidTaskTitleException);
        verify(taskInterface, never()).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void create_WhenDescriptionIsNull_ShouldThrowInvalidTaskTitleException() {
        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.create("Заголовок", null, "Комментарий", 1, 1).join());

        assertTrue(thrown.getCause() instanceof InvalidTaskDescriptionException);
        verify(taskInterface, never()).save(any(Task.class));
    }

    @Test
    void testCreate_ShouldThrow_WhenAptekaNotFound() {
        String title = "Заголовок1";
        String description = "Описание";
        String comments = "Комментарий";
        Integer aptekaId = 999;
        Integer groupId = 1;

        when(aptekaService.getOne(aptekaId)).thenThrow(AptekaNotFoundException.class);

        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.create(
                        title,
                        description,
                        comments,
                        aptekaId,
                        groupId)
                        .join());

        assertTrue(thrown.getCause() instanceof AptekaNotFoundException);

        verify(aptekaService).getOne(aptekaId);
        verifyNoInteractions(taskInterface);
    }

    @Test
    void testCreate_ShouldThrow_WhenGroupNotFound() {
        String title = "Заголовок1";
        String description = "Описание";
        String comments = "Комментарий";
        Integer aptekaId = 1;
        Integer groupId = 999;

        when(groupTaskService.getOne(groupId)).thenThrow(GroupTaskNotFoundException.class);

        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.create(
                        title,
                        description,
                        comments,
                        aptekaId,
                        groupId)
                        .join());
        assertTrue(thrown.getCause() instanceof GroupTaskNotFoundException);

        verify(groupTaskService).getOne(groupId);
        verifyNoMoreInteractions(taskInterface);
    }

    @SuppressWarnings("null")
    @Test
    void testCloseTask_ShouldReturnTask_WhenValid() {

        when(taskInterface.findById(1L)).thenReturn(Optional.of(task1));
        when(taskInterface.save(any(Task.class))).thenAnswer(invocation -> {
            Task t = invocation.getArgument(0);
            t.setStatus(TaskStatus.CLOSED);
            return t;
        });

        Task result = taskService.closeTask(1L).join();

        assertNotNull(result);
        assertEquals("CLOSED", result.getStatus());
        assertEquals(1L, result.getId());
        assertEquals("Заголовок1", result.getTitle());

        verify(taskInterface).findById(1L);
        verify(taskInterface).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void testCloseTask_ShouldThrow_WhenTaskNotFound() {
        when(taskInterface.findById(99L)).thenThrow(TaskNotFoundException.class);

        CompletionException thrown = assertThrows(
                CompletionException.class,
                () -> taskService.closeTask(99L).join());

        assertTrue(thrown.getCause() instanceof TaskNotFoundException);

        verify(taskInterface).findById(99L);
        verify(taskInterface, never()).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void update_ShouldReturnUpdatedTask_WhenValid() {
        when(taskInterface.findById(1L)).thenReturn(Optional.of(task1));

        Task updated = Task.builder()
                .id(1L)
                .title("Новый заголовок")
                .description("Новое описание")
                .comments("Новый комментарий")
                .date(task1.getDate())
                .status(TaskStatus.OPEN)
                .apteka(task1.getApteka())
                .workTask(task1.getWorkTask())
                .build();

        when(taskInterface.save(any(Task.class))).thenReturn(updated);

        Task result = taskService.update(1L, "Новый заголовок", "Новое описание", "Новый комментарий").join();

        assertEquals("Новый заголовок", result.getTitle());
        assertEquals("Новое описание", result.getDescription());
        assertEquals("Новый комментарий", result.getComments());

        verify(taskInterface).findById(1L);
        verify(taskInterface).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void update_ShouldKeepOldFields_WhenNullOrBlank() {
        when(taskInterface.findById(1L)).thenReturn(Optional.of(task1));
        when(taskInterface.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));

        Task result = taskService.update(1L, "   ", null, null).join();

        assertEquals("Заголовок1", result.getTitle());
        assertEquals("Описание", result.getDescription());
        assertEquals("Комментарий", result.getComments());

        verify(taskInterface).findById(1L);
        verify(taskInterface).save(any(Task.class));
    }

    @SuppressWarnings("null")
    @Test
    void update_ShouldThrow_WhenTaskNotFound() {
        when(taskInterface.findById(99L)).thenThrow(TaskNotFoundException.class);

        CompletionException thrown = assertThrows(
            CompletionException.class,
            () -> taskService.update(99L, "t", "d", "c").join()
        );

        assertTrue(thrown.getCause() instanceof TaskNotFoundException);
        verify(taskInterface).findById(99L);
        verify(taskInterface, never()).save(any(Task.class));
    }

    @Test
    void delete_ShouldRun_WhenTaskExists() {
        when(taskInterface.existsById(1L)).thenReturn(true);

        taskService.delete(1L).join();

        verify(taskInterface).existsById(1L);
        verify(taskInterface).deleteById(1L);
    }

    @Test
    void delete_ShouldThrow_WhenTaskNotFound() {
        when(taskInterface.existsById(1L)).thenReturn(false);

        CompletionException thrown = assertThrows(
            CompletionException.class,
            () -> taskService.delete(1L).join()
        );

        assertTrue(thrown.getCause() instanceof TaskNotFoundException);
        verify(taskInterface, never()).deleteById(anyLong());
    }
}
