package org.example.task_tracker.security.DTO.social;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
public class UnlinkSocialRequest {

    @NotBlank
    private String username;

    @NotBlank
    @ToString.Exclude
    private String password;

    @NotBlank
    private String provider;

    @NotBlank
    private String providerId;

}
