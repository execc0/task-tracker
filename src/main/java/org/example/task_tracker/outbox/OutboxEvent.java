package org.example.task_tracker.outbox;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;


@Entity
@Table(name = "outbox_events")
@Getter
@Setter
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String payload;

    private String key;

    private LocalDateTime createdAt;

    private boolean sent;

    public OutboxEvent() {

    }

    public OutboxEvent(String topic, String key, String payload) {
        this.topic = topic;
        this.key = key;
        this.payload = payload;
    }

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
