package com.ahmed.Secure.Task.Management.System.taskAttachment;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Instant;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "task_attachments",
        indexes = {
        @Index(name = "idx_task_attach_task_id_id", columnList = "task_id, id"),
        @Index(name = "idx_task_attach_task_id", columnList = "task_id"),
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_task_attach_object_key", columnNames = "object_key")        }
)
public class TaskAttachment {

    @Id
    @GeneratedValue
    private Integer id;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String objectKey;

    @Column(nullable = false)
    private TaskAttachmentStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="task_id", nullable = false)
    private Task task;

    @CreatedBy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by", nullable = false)
    private User createdBy;

    @CreationTimestamp
    private Instant createdAt;

    @Column(nullable = false)
    private Instant expiresAt;

}
