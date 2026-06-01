package org.example.task_tracker.security.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Username не может быть пустым")
    private String username;

    @NotBlank(message = "Password не может быть пустым")
    private String password;

    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Неверно указан формат email")
    private String email;
}
