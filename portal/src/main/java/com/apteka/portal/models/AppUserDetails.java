package com.apteka.portal.models;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
public class AppUserDetails implements UserDetails {

    public record RoleAndActionsInGroup(UserRole role, Set<AccountAction> actions){
        public static RoleAndActionsInGroup from(AccountRelation relation) {
            if (relation == null) {
                return null;
            }
            return new RoleAndActionsInGroup(
                    relation.getUserRole(),
                    relation.getActions()
            );
        }
    }

    private final String login;
    private final String password;
    private final Map<UserGroup, RoleAndActionsInGroup> relations;
    private final UserType type;
    private final UUID userId;
    private final String displayName;
    private final Boolean isActive;

    public AppUserDetails(Account account) {
        this.userId = account.getId();
        this.login = account.getLogin();
        this.password = account.getPassword();
        this.relations = account.getRelations().stream()
                .collect(Collectors.toMap(AccountRelation::getUserGroup, RoleAndActionsInGroup::from));
        this.isActive = account.isActive();

        UserType currentType = getUserTypeFromAccount(account);
        this.type = currentType;

        if (currentType == UserType.CLIENT) {
            Client client = account.getClient();
            this.displayName = client.getFullName();
        } else if (currentType == UserType.APTEKA) {
            Apteka apteka = account.getApteka();
            this.displayName = apteka.getAptekaName();
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
        if (relations == null || relations.isEmpty()) {
            return false;
        }

        for (RoleAndActionsInGroup roleAndActionsInGroup : relations.values()) {
            if (roleAndActionsInGroup.actions.contains(action)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAnyAction(AccountAction... checkedActions) {
        if (relations == null || relations.isEmpty()) {
            return false;
        }
        for (RoleAndActionsInGroup roleAndActionsInGroup: relations.values()) {
            for(AccountAction action: checkedActions) {
                if (roleAndActionsInGroup.actions.contains(action)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasActionInGroup(AccountAction action, UserGroup userGroup) {
        if (relations == null || relations.isEmpty()) {
            return false;
        }
        if (!relations.containsKey(userGroup)) {
            return false;
        }

        RoleAndActionsInGroup roleAndActionsInGroup = relations.get(userGroup);
        return roleAndActionsInGroup.actions.contains(action);
    }

    public boolean hasAnyActionInGroup(UserGroup userGroup, AccountAction... checkedActions) {
        if (relations == null || relations.isEmpty()) {
            return false;
        }
        if (!relations.containsKey(userGroup)) {
            return false;
        }

        RoleAndActionsInGroup roleAndActionsInGroup = relations.get(userGroup);

        for (AccountAction action : checkedActions) {
            if (roleAndActionsInGroup.actions.contains(action)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasRoleInGroup(UserRole role, UserGroup userGroup) {
        if (relations == null || relations.isEmpty()) {
            return false;
        }
        if (!relations.containsKey(userGroup)) {
            return false;
        }
        RoleAndActionsInGroup roleAndActionsInGroup = relations.get(userGroup);
        return roleAndActionsInGroup.role.equals(role);
    }

    public boolean hasRole(UserRole userRole) {
        if (relations == null || relations.isEmpty()) {
            return false;
        }
        return relations.values().stream()
                .map(RoleAndActionsInGroup::role)
                .anyMatch(role -> role == userRole);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> result = new HashSet<>();

        relations.entrySet().stream()
                .filter(entry -> entry != null && entry.getKey() != null && entry.getValue() != null)
                .forEach(entry -> {
                    String groupName = entry.getKey().getName();
                    String role = entry.getValue().role.getCode();
                    result.add(new SimpleGrantedAuthority("ROLE_" + groupName + "_" + role));

                    Set<AccountAction> actions = entry.getValue().actions;
                    if (actions != null && !actions.isEmpty()) {
                        actions.forEach(action ->
                                result.add(new SimpleGrantedAuthority("ACTION_" + groupName + "_" + action)));
                    }
                });

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
        return Boolean.TRUE.equals(isActive);
    }
}
