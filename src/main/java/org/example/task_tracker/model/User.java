package org.example.task_tracker.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    @NotBlank
    private String name;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверный формат email")
    private String email;

    public User() {
    }
}
