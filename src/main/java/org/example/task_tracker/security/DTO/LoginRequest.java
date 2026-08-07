package org.example.task_tracker.security.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username не может быть пустым")
    private String username;

    @ToString.Exclude
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}
