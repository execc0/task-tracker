package org.example.task_tracker.security.DTO.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnlinkSocialRequest {

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String provider;

    @NotBlank
    private String providerId;

}
