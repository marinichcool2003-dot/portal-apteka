package com.apteka.portal.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.services.TestData;

@ExtendWith(MockitoExtension.class)
public class GroupTaskSecurityServiceTest {

    private final GroupTaskSecurityService securityService = new GroupTaskSecurityService();

    @Test 
    void validateBossOrAdminInGroup_Success() {
        UserGroup userGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustBoss();
        assertDoesNotThrow(() -> {
            securityService.validateBossOrAdminInGroup(currentUser, userGroup);
        });
    }

    @Test
    void validateBossOrAdminInGroup_WhenNotBossOrAdmin() {
        UserGroup userGroup = TestData.defaulUserGroup();
        AppUserDetails currentUser = TestData.mockJustUser();
        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.validateBossOrAdminInGroup(currentUser, userGroup));

        assertEquals("Создать списки работ могут только начальники своего отдела", exception.getMessage());
    }

    @Test
    void validateBossOrAdminInGroup_WhenNotClient() {
        UserGroup userGroup = TestData.defaultAptekaGroup();
        AppUserDetails currentUser = TestData.mockJustApteka();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> securityService.validateBossOrAdminInGroup(currentUser, userGroup));

        assertEquals("Недопустимый тип пользователя", exception.getMessage());
    }
}
