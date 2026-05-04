package com.apteka.portal.services;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import com.apteka.portal.models.AppUserDetails;
import com.apteka.portal.models.Apteka;
import com.apteka.portal.models.Client;
import com.apteka.portal.models.GroupTask;
import com.apteka.portal.models.UserGroup;
import com.apteka.portal.models.UserRole;
import com.apteka.portal.models.WorkType;

public class TestData {

    public static UserGroup defaulUserGroup() {
        return UserGroup.builder()
                .id(1)
                .name("Розница")
                .phoneNumber("+79991112233")
                .build();
    }

    public static UserGroup newDefaulUserGroup() {
        return UserGroup.builder()
                .id(1)
                .name("АХО")
                .phoneNumber("+79991112233")
                .build();
    }

    public static UserGroup defaultAptekaGroup() {
        return UserGroup.builder()
                .id(2)
                .name("САР")
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

    public static WorkType defaultWorkType() {
        return WorkType.builder().name("Удаление накладной")
                .id(1)
                .groupTask(defaultGroupTask())
                .build();
    }

    public static WorkType newDefaultWorkType() {
        return WorkType.builder().name("Маркировка")
                .id(1)
                .groupTask(defaultGroupTask())
                .build();
    }

    public static AppUserDetails mockJustApteka() {
        Apteka apteka = mock(Apteka.class);
        lenient().when(apteka.getUserGroup()).thenReturn(defaultAptekaGroup());
        lenient().when(apteka.getRoles()).thenReturn(Set.of(UserRole.APTEKA));
        return new AppUserDetails(apteka);
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
        lenient().when(client.getRoles()).thenReturn(Set.of(UserRole.BOSS));
        lenient().when(client.getUserGroup()).thenReturn(defaulUserGroup());
        return new AppUserDetails(client);
    }

    public static AppUserDetails mockJustAdmin() {
        Client client = mock(Client.class);
        when(client.getRoles()).thenReturn(Set.of(UserRole.ADMIN));
        return new AppUserDetails(client);
    }
}
