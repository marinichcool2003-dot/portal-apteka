package com.apteka.portal.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ClientRole {

    ADMIN("ADMIN", "Администратор"),
    BOSS("ADMIN", "Админимстратор"),
    SENIOR("SENIOR", "Старший сотрудник"),
    USER("USER", "Сотрудник");

    private final String code;
    private final String description;

    public static ClientRole fromCode(String code) {
        for (ClientRole role : ClientRole.values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Неизвестный код роли: " + code);
    }
}
