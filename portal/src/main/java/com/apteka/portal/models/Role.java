package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
    LEGEND("LEGEND", "Пользователь со всеми привелегиями! Слава тебе!"),
    ADMIN("ADMIN", "Админимстратор"),
    SPEC("SPEC", "Специалист IT-отдела"),
    USER("USER", "Пользователь"),
    MANAGER("MANAGER", "Менеджер"),
    PHARMACIST("PHARMACIST", "Фармацевт");

    private final String code;
    private final String description;

    public static Role fromCode(String code) {
        for (Role role : Role.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Неизвестный код роли: " + code);
    }
}
