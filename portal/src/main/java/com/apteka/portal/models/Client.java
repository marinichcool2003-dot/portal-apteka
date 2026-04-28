package com.apteka.portal.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "client")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@ToString
public class Client implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "login", nullable = false)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Builder.Default
    @ElementCollection(targetClass = ClientRole.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "client_roles", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "role", nullable = false)
    private Set<ClientRole> roles = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private UserGroup userGroup;

    @Column(name = "avatar_url")
    private String avatarURL;

    public UUID getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public UserGroup getUserGroup() {
        return userGroup;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (roles != null) {
            roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.name())));
        }
    
        if (userGroup != null) {
            String grouprole = "GROUP_" + userGroup.getName().replace(" ", "_").toUpperCase();
            authorities.add(new SimpleGrantedAuthority(grouprole));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return login;
    }

    public boolean isAccountNonExpired() {
        return true;
    }

    public boolean isAccountNonLocked() {
        return true;
    }

    public boolean isCredentialsNonExpired() {
        return true;
    }

    public boolean isEnabled() {
        return true;
    }

}
