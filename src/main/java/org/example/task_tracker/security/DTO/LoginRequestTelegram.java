package org.example.task_tracker.security.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestTelegram {

    @NotBlank(message = "providerId не может быть пустым")
    private String providerId;

    @NotBlank(message = "chatId не может быть пустым")
    private String chatId;

    @NotBlank(message = "timestamp не может быть пустым")
    private Long timestamp;

    @NotBlank(message = "signature не может быть пустым")
    private String signature;


}
