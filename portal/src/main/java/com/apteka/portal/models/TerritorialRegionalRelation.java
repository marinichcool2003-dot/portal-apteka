package com.apteka.portal.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "territorial_regional_relation")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TerritorialRegionalRelation {
    @ManyToOne
    @JoinColumn(name = "territorial_id")
    private Client territorial;

    @ManyToOne
    @JoinColumn(name = "regional_id")
    private Client regional;
}
