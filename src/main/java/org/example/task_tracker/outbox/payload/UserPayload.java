package org.example.task_tracker.outbox.payload;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserPayload {

    private String username;
    private String name;
    private String email;

}
