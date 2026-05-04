package com.apteka.portal.components;

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

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.UserRole;

@ExtendWith(MockitoExtension.class)
public class UserGroupSecurityServiceTest {
    private final UserGroupSecurityService securityService = new UserGroupSecurityService();

    @Test
    void checkCanCreateGroup_ShouldPass_WhenUserIsAdmin() {
        AppUserDetails admin = createMockUserWithRole(UserRole.ADMIN);

        assertDoesNotThrow(() -> securityService.checkCanCreateGroup(admin));
    }

    @Test
    void checkCanCreateGroup_ShouldThrowException_WhenUserIsNotAdmin() {
        AppUserDetails user = createMockUserWithRole(UserRole.USER); 
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.checkCanCreateGroup(user));
        assertEquals("Только администратор управляет отделами", exception.getMessage());
    }

    @Test
    void checkCanCreateGroup_ShouldThrowException_WhenUserIsNull() {
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.checkCanCreateGroup(null));

        assertEquals("Пользователь не авторизован", exception.getMessage());
    }

    private AppUserDetails createMockUserWithRole(UserRole role) {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(role));
        when(client.getLogin()).thenReturn("test_user");
        return new AppUserDetails(client);
    }
}
