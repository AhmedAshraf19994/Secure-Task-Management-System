package com.ahmed.Secure.Task.Management.System.taskActivity;

import com.ahmed.Secure.Task.Management.System.task.Task;
import com.ahmed.Secure.Task.Management.System.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "task_activities")
@Entity
public class TaskActivity {

    @Id
    @GeneratedValue
    private  Integer id ;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TaskActivityType type;

    @Column(nullable = false)
    private String description;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private User actor;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "old_assignee_id")
    private User oldAssignee;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "new_assignee_id")
    private User newAssignee;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

}
