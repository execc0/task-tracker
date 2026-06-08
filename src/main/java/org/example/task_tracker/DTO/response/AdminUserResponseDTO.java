package org.example.task_tracker.DTO.response;

import lombok.Data;
import org.example.task_tracker.model.Role;

@Data
public class AdminUserResponseDTO {

    private long id;
    private String name;
    private String email;
    private String username;
    private Role role;

}
