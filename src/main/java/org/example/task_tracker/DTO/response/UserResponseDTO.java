package org.example.task_tracker.DTO.response;

import lombok.Data;

@Data
public class UserResponseDTO {

    private long id;
    private String name;
    private String email;
    private String username;

}
