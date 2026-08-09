package org.example.task_tracker.security.DTO.social.signable;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.task_tracker.security.DTO.LoginRequest;

@Data
public class LoginAndLinkRequest {

    @NotNull
    @Valid
    private LoginRequest loginRequest;

    @NotNull
    @Valid
    private LinkRequest linkRequest;


}
