package com.apteka.portal.models;

import java.time.LocalDateTime;
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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "task")
@Getter
@ToString
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "comments")
    private String comments;

    @Setter(AccessLevel.NONE)
    @Column(name = "creation_date")
    private LocalDateTime creationDate;

    @Setter
    @Column(name = "closing_date")
    private LocalDateTime closingDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private TaskPriority priority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_id", nullable = false)
    private WorkType workType;

    //================================================
    //Создатель АПТЕКА
    //================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_apteka_id")
    private Apteka createdByApteka;

    //================================================
    //Создатель СОТРУДНИК
    //================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_client_id")
    private Client createdByClient;

    //================================================
    //Исполнитель СОТРУДНИК
    //================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_client_id")
    private Client assignedClient;

    //================================================
    //Исполнитель АПТЕКА
    //================================================
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_apteka_id")
    private Apteka assignedApteka;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskComments> employeeComments;

    private Task(TaskBuilder taskBuilder) {
        this.id = taskBuilder.id;
        this.title = taskBuilder.title;
        this.description = taskBuilder.description;
        this.comments = taskBuilder.comments;
        this.updatedDate = taskBuilder.updatedDate;
        this.priority = taskBuilder.priority;
        this.workType = taskBuilder.workType;
        this.createdByApteka = taskBuilder.createdByApteka;
        this.createdByClient = taskBuilder.createdByClient;
        this.assignedClient = taskBuilder.assignedClient;
        this.assignedApteka = taskBuilder.assignedApteka;
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private Long id;
        private String title;
        private String description;
        private String comments;
        private LocalDateTime updatedDate;
        private TaskPriority priority;
        private WorkType workType;
        private Apteka createdByApteka;
        private Client createdByClient;
        private Client assignedClient;
        private Apteka assignedApteka;

        public TaskBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public TaskBuilder title(String title) {
            this.title = title;
            return this;
        }

        public TaskBuilder description(String description) {
            this.description = description;
            return this;
        }

        public TaskBuilder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public TaskBuilder workType(WorkType workType) {
            this.workType = workType;
            return this;
        }

        public TaskBuilder createdByApteka(Apteka createdByApteka) {
            this.createdByApteka = createdByApteka;
            return this;
        }

        public TaskBuilder createdByClient(Client createdByClient) {
            this.createdByClient = createdByClient;
            return this;
        }

        public TaskBuilder assignedClient(Client assignedClient) {
            this.assignedClient = assignedClient;
            return this;
        }

        public TaskBuilder assignedApteka(Apteka assignedApteka) {
            this.assignedApteka = assignedApteka;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    @PrePersist
    public void prePersist() {
        this.creationDate = LocalDateTime.now();
    }

    private void reOpen() {
        this.status = TaskStatus.OPEN;
        this.updatedDate = LocalDateTime.now();
        this.closingDate = null;
    }

    private void denied() {
        this.status = TaskStatus.DENIED;
        this.updatedDate = LocalDateTime.now();
    }

    private void processed() {
        this.status = TaskStatus.PROCESSED;
        this.updatedDate = LocalDateTime.now();
    }

    private void close() {
        this.status = TaskStatus.CLOSED;
        this.closingDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    public void changeStatus(TaskStatus newStatus) {
        if (newStatus == this.status) return;

        switch (newStatus) {
            case OPEN -> reOpen();
            case CLOSED -> close();
            case DENIED -> denied();
            case PROCESSED -> processed(); 
        }
    }
}
