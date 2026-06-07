package org.example.task_tracker.outbox;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TaskStatusPayload {

    private Long taskId;

    private String status;

    private Long userId;

}
