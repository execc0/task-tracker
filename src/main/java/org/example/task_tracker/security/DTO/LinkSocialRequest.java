package org.example.task_tracker.security.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LinkSocialRequest {

    @NotBlank
    private String provider;

    @NotBlank
    private String providerId;

    @NotBlank
    private String timestamp;

    @NotBlank
    private String signature;
}
