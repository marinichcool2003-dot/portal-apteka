package com.apteka.portal.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "task_picture")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "path")
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type")
    private AttachmentType type;

    @ManyToOne
    @JoinColumn(name = "task_comment_id")
    private TaskComment taskComment;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @PrePersist
    @PreUpdate
    private void cleanPath() {
        if (this.fileName != null) {
            this.fileName = this.fileName.strip();
        }
    }
}
