package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserRole {

    ADMIN("ADMIN", "Администратор"),
    BOSS("ADMIN", "Админимстратор"),
    SENIOR("SENIOR", "Старший сотрудник"),
    USER("USER", "Сотрудник"),
    APTEKA("APTEKA", "Аптека");

    private final String code;
    private final String description;

    public static UserRole fromCode(String code) {
        for (UserRole role : UserRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Неизвестный код роли: " + code);
    }

    public int getLevel() {
        return switch (this) {
            case APTEKA -> 0;
            case USER -> 1;
            case SENIOR -> 2;
            case BOSS -> 3;
            case ADMIN -> 4;
            default -> -1;
        };
    }
}
