package org.example.task_tracker.security.DTO.social.signable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.task_tracker.security.DTO.LoginRequest;

@Data
public class LoginAndLinkRequest {

    @NotNull
    private LoginRequest loginRequest;

    @NotNull
    private LinkRequest linkRequest;


}
