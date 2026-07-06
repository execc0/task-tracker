package org.example.task_tracker.security.DTO.social.signable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LinkRequest implements Signable {

    @NotBlank
    private String provider;

    @NotBlank
    private String providerId;

    @NotNull
    private Long timestamp;

    @NotBlank
    private String signature;

    @Override
    public List<Object> getSignableFields() {
        return List.of(provider, providerId, timestamp);
    }
}
