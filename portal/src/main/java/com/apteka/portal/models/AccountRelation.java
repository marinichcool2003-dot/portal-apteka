package com.apteka.portal.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table(name = "account_user_group_relation")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountRelation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "user_group_id")
    private UserGroup userGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private UserRole userRole;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "account_actions", joinColumns = @JoinColumn(name = "relation_id"))
    @Column(name = "action", nullable = false)
    private Set<AccountAction> actions;
}
