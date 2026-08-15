package com.apteka.portal.models;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "account")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Account {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "login", nullable = false, unique = true, length = 50)
    private String login;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Client client;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Apteka apteka;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    Set<AccountRelation> relations;

    @Column(name = "is_active")
    private boolean isActive;
}
