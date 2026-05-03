package com.apteka.portal.services;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.components.UserGroupSecurityService;
import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

@ExtendWith(MockitoExtension.class)
public class UserGroupSecurityServiceTest {
    private final UserGroupSecurityService securityService = new UserGroupSecurityService();

    @Test
    void checkCanCreateGroup_ShouldPass_WhenUserIsAdmin() {
        // Arrange
        AppUserDetails admin = createMockUserWithRole(UserRole.ADMIN);

        // Act & Assert
        // Если метод ничего не выбрасывает — тест пройден
        assertDoesNotThrow(() -> securityService.checkCanCreateGroup(admin));
    }

    @Test
    void checkCanCreateGroup_ShouldThrowException_WhenUserIsNotAdmin() {
        // Arrange
        AppUserDetails user = createMockUserWithRole(UserRole.USER); // Обычный пользователь

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.checkCanCreateGroup(user));

        assertEquals("Только администратор управляет отделами", exception.getMessage());
    }

    @Test
    void checkCanCreateGroup_ShouldThrowException_WhenUserIsNull() {
        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.checkCanCreateGroup(null));

        assertEquals("Пользователь не авторизован", exception.getMessage());
    }

    // Вспомогательный метод для создания пользователя с нужной ролью
    private AppUserDetails createMockUserWithRole(UserRole role) {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(role));
        when(client.getLogin()).thenReturn("test_user");
        return new AppUserDetails(client);
    }
}
