package com.apteka.portal.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Setter;
import lombok.ToString;

@Setter
@ToString
public class AppUserDetails implements UserDetails {

    private final String login;
    private final String password;
    private final Set<UserRole> roles;
    private final UserGroup userGroup;
    private final UserType type;

    private final UUID clientId;
    private final Integer aptekaId;

    public AppUserDetails(Client client) {
        this.login = client.getLogin();
        this.password = client.getPassword();
        this.roles = client.getRoles();
        this.userGroup = client.getUserGroup();
        this.type = UserType.CLIENT;
        this.clientId = client.getClientId();
        this.aptekaId = null;
    }

    public AppUserDetails(Apteka apteka) {
        this.login = apteka.getLogin();
        this.password = apteka.getPassword();
        this.roles = Set.of(UserRole.APTEKA);
        this.userGroup = apteka.getUserGroup();
        this.type = UserType.APTEKA;
        this.aptekaId = apteka.getAptekaId();
        this.clientId = null;
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

    public Set<UserRole> getRoles() {
        return roles;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    public UserType getType() {
        return type;
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
