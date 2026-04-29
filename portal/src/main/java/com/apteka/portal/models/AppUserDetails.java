package com.apteka.portal.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
    private final UUID clientId;
    private final Integer aptekaId;
    private final String displayName;

    public AppUserDetails(Client client) {
        this.login = client.getLogin();
        this.password = client.getPassword();
        this.roles = client.getRoles();
        this.userGroup = client.getUserGroup();
        this.type = UserType.CLIENT;
        this.clientId = client.getId();
        this.aptekaId = null;
        this.displayName = client.getFullName();
    }

    public AppUserDetails(Apteka apteka) {
        this.login = apteka.getLogin();
        this.password = apteka.getPassword();
        this.roles = Set.of(UserRole.APTEKA);
        this.userGroup = apteka.getUserGroup();
        this.type = UserType.APTEKA;
        this.aptekaId = apteka.getId();
        this.clientId = null;
        this.displayName = apteka.getUserGroup().getName() + " " + apteka.getNumber();
    }

    public Object getInternalId() {
        return (type == UserType.CLIENT) ? clientId : aptekaId;
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
