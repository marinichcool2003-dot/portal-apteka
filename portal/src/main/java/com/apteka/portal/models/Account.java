package com.apteka.portal.models;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "phone_number", unique = true, length = 20)
    private String phoneNumber;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Client client;

    @OneToOne(mappedBy = "account", fetch = FetchType.LAZY)
    private Apteka apteka;    

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private UserGroup userGroup;
}
