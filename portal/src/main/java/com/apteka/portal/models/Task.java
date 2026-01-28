package com.apteka.portal.models;

import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "task")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "comments")
    private String comments;

    @Column(name = "date")
    private Date date;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "status", nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "priority", nullable = false)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_apteka_id")
    private Apteka createdByApteka;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_client_id")
    private Client createdByClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_id", nullable = false)
    private WorkType workType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_client_id")
    private Client assignedClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_apteka_id")
    private Apteka assignedApteka;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskComments> employeeComments;
}
