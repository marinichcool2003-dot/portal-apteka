package com.apteka.portal.models;

import com.apteka.portal.exceptions.UnknowRoleException;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Роль пользователя")
@Getter
@AllArgsConstructor
public enum UserRole {
    @Schema(description = "Администратор")
    ADMIN("ADMIN", "Администратор", 3),
    @Schema(description = "Босс")
    BOSS("BOSS", "Босс", 2),
    @Schema(description = "Сотрудник")
    USER("USER", "Сотрудник", 1),
    @Schema(description = "Аптека")
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
