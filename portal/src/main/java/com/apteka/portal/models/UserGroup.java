package com.apteka.portal.models;

import java.time.Instant;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "group_user")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "internal_number", length = 20)
    private String internalNumber;

    @Column(name = "extension_number", length = 20)
    private String extensionNumber;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @ManyToMany
    @JoinTable(
        name = "group_group_visibility", 
        joinColumns = @JoinColumn(name = "first_group_id"), 
        inverseJoinColumns = @JoinColumn(name = "second_group_id")
    )
    private Set<UserGroup> visibleGroups;

    @Column(name = "is_active")
    private boolean isActive;
}
