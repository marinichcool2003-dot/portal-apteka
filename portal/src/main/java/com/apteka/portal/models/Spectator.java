package com.apteka.portal.models;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "spectator")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Spectator {
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "account_id")
    private Account account;
}
