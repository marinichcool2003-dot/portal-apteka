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

import com.apteka.portal.exceptions.DublicateGroupAptekiException;
import com.apteka.portal.exceptions.GroupAptekiNotFoundException;
import com.apteka.portal.exceptions.InvalidGroupAptekiException;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.repository.GroupAptekiInterface;
import com.apteka.portal.services.GroupAptekiServiceTest;

public class GroupAptekiServiceTest {
    @Mock
    private GroupAptekiInterface groupAptekiInterface;

    @InjectMocks
    private GroupAptekiService groupAptekiService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAll_ShouldReturnList(){
        List<GroupApteki> mockList = List.of(
            new GroupApteki(1, "Группа 1"),
            new GroupApteki(1, "Группа 2")
        );

        when(groupAptekiInterface.findAll()).thenReturn(mockList);

        List<GroupApteki> result = groupAptekiService.getAll();

        assertEquals(2, result.size());
        verify(groupAptekiInterface, times(1)).findAll();
    }

    @Test
    void testGetOne_ShouldReturnGroupApteki_WhenExists(){
        GroupApteki mockGroupApteki = new GroupApteki(1, "Группа 1");

        when(groupAptekiInterface.findById(1)).thenReturn(Optional.of(mockGroupApteki));

        GroupApteki result = groupAptekiService.getOne(1);

        assertEquals("Группа 1", result.getName());
        verify(groupAptekiInterface).findById(1);
    }

    @Test
    void testGetOne_ShouldThrow_WhenNotFound(){
        when(groupAptekiInterface.findById(99)).thenReturn(Optional.empty());

        assertThrows(GroupAptekiNotFoundException.class, () -> groupAptekiService.getOne(99));
        verify(groupAptekiInterface).findById(99);
    }

    @SuppressWarnings("null")
    @Test 
    void testCreate_ShouldSave_WhenValid(){
        String name = "Новая группа";
        GroupApteki saved = GroupApteki.builder().id(1).name(name).build();

        when(groupAptekiInterface.findByName(name)).thenReturn(Optional.empty());
        when(groupAptekiInterface.save(any(GroupApteki.class))).thenReturn(saved);

        GroupApteki result = groupAptekiService.create(name);

        assertEquals(name, result.getName());
        verify(groupAptekiInterface).findByName(name);
        verify(groupAptekiInterface).save(any(GroupApteki.class));
    }

    @Test
    void testCreate_ShouldThrow_WhenNameIsNullOrEmpty(){
        assertThrows(InvalidGroupAptekiException.class, () -> groupAptekiService.create(""));
        assertThrows(InvalidGroupAptekiException.class, () -> groupAptekiService.create("   "));
    }

    @Test
    void testCreate_ShouldThrow_WnenDublicate(){
        String name = "Dublicate";
        when(groupAptekiInterface.findByName(name)).thenReturn(Optional.of(new GroupApteki(1, name)));

        assertThrows(DublicateGroupAptekiException.class, () -> groupAptekiService.create(name));
        verify(groupAptekiInterface).findByName(name);
    }

    @Test
    void testDelete_ShouldThrow_WhenExists(){
        when(groupAptekiInterface.existsById(1)).thenReturn(true);

        groupAptekiService.delete(1);

        verify(groupAptekiInterface).deleteById(1);
    }

    @SuppressWarnings("null")
    @Test
    void testDelete_ShouldThrow_WhenNotExists(){
        when(groupAptekiInterface.existsById(999)).thenReturn(false);

        assertThrows(GroupAptekiNotFoundException.class, () -> groupAptekiService.delete(999));
        verify(groupAptekiInterface).existsById(999);
        verify(groupAptekiInterface, never()).deleteAllById(any());
    }
}
