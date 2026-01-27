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
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apteka.portal.exceptions.AptekaNotFoundException;
import com.apteka.portal.exceptions.DublicateAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaLoginException;
import com.apteka.portal.exceptions.InvalidAptekaPasswordException;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.GroupApteki;
import com.apteka.portal.repository.AptekaInterface;

public class AptekaServiceTest {
    @Mock
    private AptekaInterface aptekaInterface;

    @Mock
    private GroupAptekiService groupAptekiService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AptekaService aptekaService;

    private List<Apteka> mockList;
    private GroupApteki mockGroup1;
    private GroupApteki mockGroup2;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockList = List.of(
                new Apteka(1, "sacapteka101@farmp.ru", "15943245", 001, "Ростов-на-Дону", "9881233322", mockGroup1),
                new Apteka(2, "sacapteka102@farmp.ru", "33636336", 002, "Ростов-на-Дону", "9881233321", mockGroup2));
        mockGroup1 = new GroupApteki(1, "Группа1");
        mockGroup2 = new GroupApteki(2, "Группа2");
    }

    @Test
    void testGetAll_ShouldReturnList() {
        when(aptekaInterface.findAll()).thenReturn(mockList);

        List<Apteka> result = aptekaService.getAll();

        assertEquals(2, result.size());
        assertEquals("sacapteka102@farmp.ru", result.get(1).getLogin());
        verify(aptekaInterface, times(1)).findAll();
    }

    @Test
    void testGetOne_ShouldReturnApteka_WhenExists() {
        Apteka mockApteka = Apteka.builder().id(1)
                .login("sacapteka101@farmp.ru")
                .password("15943245")
                .group(new GroupApteki())
                .build();

        when(aptekaInterface.findById(1)).thenReturn(Optional.of(mockApteka));

        Apteka result = aptekaService.getOne(1);
        assertEquals("sacapteka101@farmp.ru", result.getLogin());
        verify(aptekaInterface).findById(1);
    }

    @Test
    void testGetOne_ShouldThrow_WhenNotFound() {
        when(aptekaInterface.findById(999)).thenReturn(Optional.empty());

        assertThrows(AptekaNotFoundException.class, () -> aptekaService.getOne(999));
        verify(aptekaInterface).findById(999);
    }

    @SuppressWarnings("null")
    @Test
    void testCreate_ShouldSave_WhenValid() {

        String login = "sacapteka101@farmp.ru";
        String password = "15943245";
        Integer number = 001;
        String adress = "Ростов-на-Дону";
        String phoneNumber = "9881233322";
        Integer group_id = 1;
        GroupApteki mockGroupApteki = new GroupApteki(group_id, "Group Apteki1");

        when(aptekaInterface.existsByLogin(login)).thenReturn(false);
        when(groupAptekiService.getOne(group_id)).thenReturn(mockGroupApteki);
        when(passwordEncoder.encode(password)).thenReturn("success");

        Apteka saved = Apteka.builder().id(1)
                .login(login)
                .password("success")
                .number(number)
                .adress(adress)
                .phoneNumber(phoneNumber)
                .group(mockGroupApteki)
                .build();

        when(aptekaInterface.save(any(Apteka.class))).thenReturn(saved);

        Apteka result = aptekaService.create(
                login,
                password,
                number,
                adress,
                phoneNumber,
                group_id);

        assertEquals("success", result.getPassword());
        assertEquals("sacapteka101@farmp.ru", result.getLogin());
        assertEquals(mockGroupApteki, result.getGroup());

        verify(aptekaInterface).existsByLogin(login);
        verify(passwordEncoder).encode(password);
        verify(aptekaInterface).save(any(Apteka.class));
    }

    @Test
    void testCreate_ShouldThrow_WhenLoginIsNullOrEmpty() {
        assertThrows(InvalidAptekaLoginException.class,
                () -> aptekaService.create("", "password", 001, "Камчатка", "9881233322", 1));
    }

    @Test
    void testCreate_ShouldThrow_WhenPasswordIsNullOrEmpty() {
        assertThrows(InvalidAptekaPasswordException.class,
                () -> aptekaService.create("login", "", 001, "Камчатка", "9881233322", 1));
    }

    @Test
    void testCreate_ShouldThrow_WhenLoginExists() {
        when(aptekaInterface.existsByLogin("login")).thenReturn(true);
        assertThrows(DublicateAptekaLoginException.class,
                () -> aptekaService.create("login", "password", 001, "Камчатка", "9881233322",1));
        verify(aptekaInterface).existsByLogin("login");
    }

    @Test
    void testDelete_ShouldRemove_WhenExists() {
        when(aptekaInterface.existsById(1)).thenReturn(true);
        aptekaService.delete(1);
        verify(aptekaInterface).deleteById(1);
    }

    @Test
    void testDelete_ShouldThrows_WhenNotFound() {
        when(aptekaInterface.existsById(999)).thenReturn(false);
        assertThrows(AptekaNotFoundException.class,
                () -> aptekaService.delete(999));
        verify(aptekaInterface, never()).deleteById(999);
    }
}
