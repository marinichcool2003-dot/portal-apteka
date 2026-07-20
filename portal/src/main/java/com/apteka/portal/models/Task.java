package com.apteka.portal.models;

import java.time.Instant;
import java.util.Set;

import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "task")
@Getter
@ToString(onlyExplicitlyIncluded = true)
@Setter
@DynamicInsert
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @ToString.Include
    private Long id;

    @Column(name = "title", nullable = false)
    @ToString.Include
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @CreatedDate
    @Setter(AccessLevel.NONE)
    @Column(name = "creation_date", nullable = false, updatable = false)
    private Instant creationDate;

    @Setter
    @Column(name = "closing_date")
    private Instant closingDate;

    @LastModifiedDate
    @Column(name = "updated_date")
    private Instant updatedDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_type_id", nullable = false)
    private WorkType workType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private Account creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigner_id")
    private Account assigner;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TaskComment> employeeComments;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<TaskPicture> pictures;

    private Task(TaskBuilder taskBuilder) {
        this.id = taskBuilder.id;
        this.title = taskBuilder.title;
        this.description = taskBuilder.description;
        this.updatedDate = taskBuilder.updatedDate;
        this.workType = taskBuilder.workType;
        this.creator = taskBuilder.creator;
        this.assigner = taskBuilder.assigner;
        this.status = TaskStatus.OPEN;
    }

    public static TaskBuilder builder() {
        return new TaskBuilder();
    }

    public static class TaskBuilder {
        private Long id;
        private String title;
        private String description;
        private Instant updatedDate;
        private WorkType workType;
        private Account creator;
        private Account assigner;

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

        public TaskBuilder workType(WorkType workType) {
            this.workType = workType;
            return this;
        }

        public TaskBuilder creator(Account creator) {
            this.creator = creator;
            return this;
        }

        public TaskBuilder createdByClient(Account assigner) {
            this.assigner = assigner;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    private void reOpen() {
        this.status = TaskStatus.OPEN;
        this.closingDate = null;
    }

    private void denied() {
        this.status = TaskStatus.DENIED;
    }

    private void processed() {
        this.status = TaskStatus.PROCESSED;
    }

    private void close() {
        this.status = TaskStatus.CLOSED;
        this.closingDate = Instant.now();
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
