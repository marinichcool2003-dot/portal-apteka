package com.apteka.portal.models;

import java.util.Set;
import java.util.UUID;

public interface UsersInApp {
    UserGroup getUserGroup();

    boolean isApteka();

    boolean isClient();

    UUID getClientId();

    Integer getAptekaId();

    Set<UserRole> getRoles();

    default boolean hasRole(UserRole role) {
        return getRoles().contains(role);
    }

    default boolean isJustUser() {
        return getRoles().equals(Set.of(UserRole.USER));
    }
}
