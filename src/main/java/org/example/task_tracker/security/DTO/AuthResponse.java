package org.example.task_tracker.security.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class AuthResponse {

    @ToString.Exclude
    private String token;

}
