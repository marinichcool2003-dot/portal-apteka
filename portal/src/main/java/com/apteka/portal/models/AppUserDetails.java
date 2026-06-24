package com.apteka.portal.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private final UserRole role;
    private final Set<AccountAction> actions;
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
            this.role = account.getUserRole();
            this.actions = account.getActions();
            this.displayName = client.getFullName();
        } else if (getUserTypeFromAccount(account) == UserType.APTEKA) {
            this.type = UserType.APTEKA;
            Apteka apteka = account.getApteka();
            this.displayName = account.getUserGroup().getName() + " " + apteka.getNumber();
            this.role = UserRole.APTEKA;
            this.actions = null;
        } else {
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

    public boolean isApteka() {
        return type == UserType.APTEKA;
    }

    public boolean isClient() {
        return type == UserType.CLIENT;
    }

    public boolean hasAction(AccountAction action) {
        return actions.contains(action);
    }

    public boolean hasAnyAction(AccountAction... checkedActions) {
        for (AccountAction action : checkedActions) {
            if (actions.contains(action)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> result = new ArrayList<>();

        result.add(new SimpleGrantedAuthority("GROUP_" + userGroup.getName()));

        result.add(new SimpleGrantedAuthority("ROLE_" + role.name()));

        actions.forEach(a -> result.add(new SimpleGrantedAuthority("ACTION_" + a.name())));

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
