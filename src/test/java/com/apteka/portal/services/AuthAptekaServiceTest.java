package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apteka.portal.models.Apteka;
import com.apteka.portal.repository.AptekaInterface;
import com.apteka.portal.security.JwtTokenProvider;

public class AuthAptekaServiceTest {
    @Mock
    private AptekaInterface aptekaInterface;

    @Mock
    private AptekaService aptekaService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthAptekaService authAptekaService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_ShouldReturnJwtToken() {
        String login = "apteka1";
        String password = "pass123";
        Integer number = 001;
        String adress = "Ростов-на-Дону";
        String phoneNumber = "9881231122";
        Integer groupId = 5;

        Apteka apteka = new Apteka();
        apteka.setLogin(login);
        apteka.setPassword(password);

        when(aptekaService.create(login, password, number, adress, phoneNumber, groupId)).thenReturn(apteka);
        when(jwtTokenProvider.generateToken(login)).thenReturn("jwt-token-123");

        String token = authAptekaService.register(login, password, number, adress, phoneNumber, groupId);

        assertEquals("jwt-token-123", token);
        verify(aptekaService).create(login, password, number, adress, phoneNumber, groupId);
        verify(jwtTokenProvider).generateToken(login);
    } 

    @Test
    void login_ShouldReturnJwtToken_WhenCredentialsAreValid() {
        String login = "apteka1";
        String password = "pass123";
        String encodedPassword = "$2a$10$encoded";

        Apteka apteka = new Apteka();
        apteka.setLogin(login);
        apteka.setPassword(encodedPassword);

        when(aptekaInterface.findByLogin(login)).thenReturn(Optional.of(apteka));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtTokenProvider.generateToken(login)).thenReturn("jwt-token-456");

        String token = authAptekaService.login(login, password);

        assertEquals("jwt-token-456", token);
        verify(aptekaInterface).findByLogin(login);
        verify(passwordEncoder).matches(password, encodedPassword);
        verify(jwtTokenProvider).generateToken(login);
    }

    @Test
    void login_ShouldThrowException_WhenAptekaNotFound() {
        when(aptekaInterface.findByLogin("unknown")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authAptekaService.login("unknown", "1234")
        );

        assertEquals("Аптека не найдена!", exception.getMessage());
    }

    @Test
    void login_ShouldThrowException_WhenPasswordIsInvalid() {
        String login = "apteka1";
        String password = "wrong";
        String encodedPassword = "$2a$10$encoded";

        Apteka apteka = new Apteka();
        apteka.setLogin(login);
        apteka.setPassword(encodedPassword);

        when(aptekaInterface.findByLogin(login)).thenReturn(Optional.of(apteka));
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                authAptekaService.login(login, password)
        );

        assertEquals("Неверный пароль!", exception.getMessage());
    }
}
