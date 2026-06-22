package com.apteka.portal.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class AppUserDetails implements UserDetails {

    private final String login;
    private final String password;
    private final Set<UserRole> roles;
    private final UserGroup userGroup;
    private final UserType type;
    private final UUID userId;
    private final String displayName;

    public AppUserDetails(Account account) {
        this.userId = account.getId();
        this.login = account.getLogin();
        this.password = account.getPassword();
        this.userGroup = account.getUserGroup();

        if (getUserTypeFromAccount(account) == UserType.CLIENT) {
            this.type = UserType.CLIENT;
            Client client = account.getClient();
            this.roles = client.getRoles();
            this.displayName = client.getFullName();
        }
        else if (getUserTypeFromAccount(account) == UserType.APTEKA) {
            this.type = UserType.APTEKA;
            Apteka apteka = account.getApteka();
            this.displayName = account.getUserGroup().getName() + " " + apteka.getNumber();
        }
        else {
            throw new AccessDeniedException("Не удалось идентифицировать тип пользователя!");
        }
    }

    private UserType getUserTypeFromAccount(Account account) {
        if (account.getClient() != null) {
            return UserType.CLIENT;
        } else if (account.getApteka() != null) {
            return UserType.APTEKA;
        } else {
            throw new AccessDeniedException("Не удалось идентифицировать тип пользователя!");
        }
    }

    public UUID getInternalId() {
        return userId;
    }

    public boolean isJustUser() {
        if (type == UserType.APTEKA)
            return false;
        return roles.stream()
                .noneMatch(role -> role == UserRole.ADMIN || role == UserRole.BOSS || role == UserRole.SENIOR);
    }

    public boolean isApteka() {
        return type == UserType.APTEKA;
    }

    public boolean isClient() {
        return type == UserType.CLIENT;
    }

    public boolean hasRole(UserRole role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(UserRole... targetRoles) {
        for (UserRole target : targetRoles) {
            if (roles.contains(target))
                return true;
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> result = new ArrayList<>();

        roles.forEach(r -> result.add(new SimpleGrantedAuthority("ROLE_" + r.name())));

        if (userGroup != null) {
            result.add(new SimpleGrantedAuthority("GROUP_" + userGroup.getName()));
        }

        return result;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
