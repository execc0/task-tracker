package org.example.task_tracker.security.DTO.social.signable;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.task_tracker.security.DTO.RegisterRequest;


@Data
public class RegisterAndLinkRequest {

    @NotNull
    private RegisterRequest registerRequest;

    @NotNull
    private LinkRequest linkRequest;

}
