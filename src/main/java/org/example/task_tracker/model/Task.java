package org.example.task_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "tasks")
@Getter
@Setter
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    @NotBlank(message = "Название не может быть пустым")
    private String title;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Enumerated(EnumType.STRING)
    private Priority priority;
    @Column(updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime deadline;
    private String description;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Task() {}

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = Status.TODO;
        }
        if (priority == null) {
            priority = Priority.LOW;
        }
    }

}
