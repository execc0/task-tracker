package org.example.task_tracker.security.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequestTelegram {

    @NotNull
    private RegisterRequest registerRequest;

    @NotBlank(message = "chatId не может быть пустым")
    private String chatId;

    @NotBlank(message = "JWTtoken не может быть пустым")
    private String JWTtoken;

}
