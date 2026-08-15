package com.apteka.portal.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipment")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Equipment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "apteka_id")
    private Apteka apteka;

    @ManyToOne
    @JoinColumn(name = "type_id")
    private EquipmentType type;
}
