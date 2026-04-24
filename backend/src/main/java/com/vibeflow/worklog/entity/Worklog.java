package com.vibeflow.worklog.entity;

import com.vibeflow.auth.entity.User;
import com.vibeflow.task.entity.Task;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "worklogs")
@Immutable
@Getter
@Setter
public class Worklog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal hours;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Worklog() {}

    public Worklog(Task task, User user, BigDecimal hours) {
        this.task = task;
        this.user = user;
        this.hours = hours;
    }
}
