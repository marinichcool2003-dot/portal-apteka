package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowRoleException;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {
    ADMIN("ADMIN", "Администратор", 3),
    BOSS("BOSS", "Босс", 2),
    USER("USER", "Сотрудник", 1),
    APTEKA("APTEKA", "Аптека", 0);

    private final String code;
    private final String description;
    private final int level;

    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }

        throw new UnknowRoleException("Неизвестный код роли: " + code);
    }

    public int getLevel() {
        return this.level;
    }

    public boolean isHigherThan(UserRole other) {
        return this.level > other.level;
    }

    public boolean isHigherOrEqual(UserRole other) {
        return this.level >= other.level;
    }
}
