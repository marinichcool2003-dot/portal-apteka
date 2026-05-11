package com.apteka.portal.components;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.services.TestData;

@ExtendWith(MockitoExtension.class)
public class ClientSecurityServiceTest {
    @InjectMocks
    private ClientSecurityService clientSecurityService;

    @Test
    void validateHasElevatedPrivelegesInGroup_Success() {
        AppUserDetails currentUser = TestData.mockJustSenior();
        UserGroup userGroup = TestData.defaulUserGroup();

        assertDoesNotThrow(
            () -> clientSecurityService.validateHasElevatedPrivelegesInGroup(
                currentUser, 
                userGroup.getId()));

    }

    @Test 
    void validateHasElevatedPrivelegesInGroup_AnotherGroup() {
        AppUserDetails currentUser = TestData.mockJustSenior();
        UserGroup userGroup = TestData.newDefaulUserGroup();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> clientSecurityService.validateHasElevatedPrivelegesInGroup(currentUser, userGroup.getId())
        );

        assertEquals("У вас нет прав на просмотр данных сотрудника", exception.getMessage());
    }

    @Test
    void validateWhoCanSelectClients_WhenApteka() {
        AppUserDetails currentUser = TestData.mockJustApteka();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> clientSecurityService.validateWhoCanSelectClients(currentUser)
        );

        assertEquals("У вас нет прав на просмотр данных сотрудника", exception.getMessage());
    }

    @Test
    void validateCanCreateClient_WhenBoss_Success() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.newDefaulUserGroup();

        assertDoesNotThrow(
            () -> clientSecurityService.validateCanCreateClient(currentUser, userGroup.getId()));
    }

    @Test 
    void validateCanCreateClient_WhenBoss_AnotherGroup() {
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.defaulUserGroup();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> clientSecurityService.validateCanCreateClient(currentUser, userGroup.getId())
        );

        assertEquals("У вас нет права создавать сотрудников", exception.getMessage());
    }

    @Test
    void canGiveRoleToClient_WhenBoss_Success() {
        Set<UserRole> newRoles = Set.of(UserRole.SENIOR);
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.newDefaulUserGroup();

        assertDoesNotThrow(
            () -> clientSecurityService.canGiveRoleToClient(newRoles, currentUser, userGroup));
    }

    @Test
    void canGiveRoleToClient_WhenBoss_AnotherGroup() {
        Set<UserRole> newRoles = Set.of(UserRole.SENIOR);
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.defaulUserGroup();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> clientSecurityService.canGiveRoleToClient(newRoles, currentUser, userGroup));

        assertEquals("Можно работать только в своей группе", exception.getMessage());
    }

    @Test
    void canGiveRoleToClient_WhenUser() {
        Set<UserRole> newRoles = Set.of(UserRole.USER);
        AppUserDetails currentUser = TestData.mockJustUser();
        UserGroup userGroup = TestData.defaulUserGroup();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
            () -> clientSecurityService.canGiveRoleToClient(newRoles, currentUser, userGroup));
        
        assertEquals("USER не может назначать роли", exception.getMessage());
    }

    @Test
    void canGiveRoleToClient_WhenBoss_GiveBoss() {
        Set<UserRole> newRoles = Set.of(UserRole.BOSS);
        AppUserDetails currentUser = TestData.mockJustBoss();
        UserGroup userGroup = TestData.newDefaulUserGroup();

        AccessDeniedException exception = assertThrows(AccessDeniedException.class, 
            () -> clientSecurityService.canGiveRoleToClient(newRoles, currentUser, userGroup)
        );

        assertEquals("Нельзя назначать роль выше или равную своей", exception.getMessage());
    }
}
