package org.example.task_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Data
@Table(name = "socials_auth")
public class Social {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull(message = "userId не может быть пустым")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    @NotBlank(message = "provider не может быть пустым")
    private String provider;

    @Column(nullable = false)
    @NotBlank(message = "providerId не может быть пустым")
    private String providerId;

}
