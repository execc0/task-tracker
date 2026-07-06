package org.example.task_tracker.security.DTO.social.signable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LoginRequestTelegram implements Signable {

    @NotBlank
    private String chatId;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String signature;


    @Override
    public List<Object> getSignableFields() {
        return List.of(chatId, timestamp);
    }

}
