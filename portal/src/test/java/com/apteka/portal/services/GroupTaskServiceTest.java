package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.apteka.portal.exceptions.DublicateGroupTaskException;
import com.apteka.portal.exceptions.GroupTaskNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupTaskException;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.repository.GroupTaskRepository;

public class GroupTaskServiceTest {
    @Mock
    private GroupTaskRepository groupTaskInterface;

    @InjectMocks
    private GroupTaskService groupTaskService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll_ShouldReturnList(){
        List<GroupTask> mockList = List.of(
            new GroupTask(1, "Группа 1"),
            new GroupTask(2, "Группа 2")
        );

        when(groupTaskInterface.findAll()).thenReturn(mockList);

        List<GroupTask> result = groupTaskService.getAll();

        assertEquals(2, result.size());
        verify(groupTaskInterface, times(1)).findAll();
    }

    @Test
    void testGetOne_ShouldReturnGroupTask_WhenExists(){
        GroupTask mockGroupTask = new GroupTask(1, "Группа 1");

        when(groupTaskInterface.findById(1)).thenReturn(Optional.of(mockGroupTask));

        GroupTask result = groupTaskService.getOne(1);

        assertEquals("Группа 1", result.getName());
        verify(groupTaskInterface).findById(1);
    }

    @Test
    void testGetOne_ShouldThrow_WhenNotFound(){
        when(groupTaskInterface.findById(99)).thenReturn(Optional.empty());

        assertThrows(GroupTaskNotFoundException.class, () -> groupTaskService.getOne(99));
        verify(groupTaskInterface).findById(99);
    }

    @SuppressWarnings("null")
    @Test
    void testCreate_ShouldReturnGroupTask_WhenValid(){
        String name = "Новая группа";
        GroupTask saved = GroupTask.builder().id(1).name(name).build();

        when(groupTaskInterface.findByName(name)).thenReturn(Optional.empty());
        when(groupTaskInterface.save(any(GroupTask.class))).thenReturn(saved);

        GroupTask result = groupTaskService.create(name);

        assertEquals(name, result.getName());
        verify(groupTaskInterface).findByName(name);
        verify(groupTaskInterface).save(any(GroupTask.class));
    }

    @Test
    void testCreate_ShouldThrow_WhenNameIsNullOrEmpty(){
        assertThrows(InvalidGroupTaskException.class, () -> groupTaskService.create(""));
        assertThrows(InvalidGroupTaskException.class, () -> groupTaskService.create("   "));
    }

    @Test
    void testCreate_ShouldThrow_WhenDublicate(){
        String name = "Dublicate";
        when(groupTaskInterface.findByName(name)).thenReturn(Optional.of(new GroupTask(1, name)));

        assertThrows(DublicateGroupTaskException.class, () -> groupTaskService.create(name));
        verify(groupTaskInterface).findByName(name);
    }

    @Test
    void testDelete_ShouldRemove_WhenExists() {
        when(groupTaskInterface.existsById(1)).thenReturn(true);

        groupTaskService.delete(1);

        verify(groupTaskInterface).deleteById(1);
    }

    @SuppressWarnings("null")
    @Test
    void testDelete_ShouldThrow_WhenNotExists(){
        when(groupTaskInterface.existsById(999)).thenReturn(false);

        assertThrows(GroupTaskNotFoundException.class, () -> groupTaskService.delete(999));
        verify(groupTaskInterface).existsById(999);
        verify(groupTaskInterface, never()).deleteById(any());
    }
}
