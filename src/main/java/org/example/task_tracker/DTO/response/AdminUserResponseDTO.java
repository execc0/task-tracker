package org.example.task_tracker.DTO.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.task_tracker.model.Role;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserResponseDTO {

    private long id;
    private String name;
    private String email;
    private String username;
    private Role role;

}
