package com.apteka.portal.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;

public class TestData {

    // Группа пользователей
    public static UserGroup defaulUserGroup() {
        return UserGroup.builder()
                .id(1)
                .name("Розница")
                .phoneNumber("+79991112233")
                .build();
    }

    public static UserGroup newDefaulUserGroup() {
        return UserGroup.builder()
                .id(2)
                .name("АХО")
                .phoneNumber("+79991112233")
                .build();
    }

    // Группа задач
    public static GroupTask defaultGroupTask() {
        return GroupTask.builder().name("Накладные")
                .id(1)
                .userGroup(defaulUserGroup())
                .build();
    }

    public static GroupTask newGroupTask() {
        return GroupTask.builder().name("Алгоритм")
                .id(2)
                .userGroup(defaulUserGroup())
                .build();
    }

    public static AppUserDetails mockJustApteka() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.APTEKA));
        return new AppUserDetails(client);
    }

    public static AppUserDetails mockJustUser() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.USER));
        return new AppUserDetails(client);
    }

    public static AppUserDetails mockJustSenior() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.SENIOR));
        return new AppUserDetails(client);
    }

    public static AppUserDetails mockJustBoss() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.BOSS));
        return new AppUserDetails(client);
    }

    public static AppUserDetails mockJustAdmin() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.ADMIN));
        return new AppUserDetails(client);
    }
}
