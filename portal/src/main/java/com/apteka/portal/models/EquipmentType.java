package com.apteka.portal.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "equipment_type")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
public class EquipmentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Short id;

    @Column(name = "name")
    private String name;
}
