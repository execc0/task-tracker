package org.example.task_tracker.DTO.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.example.task_tracker.model.Priority;
import org.example.task_tracker.model.Status;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponseDTO {

    private long id;
    private String title;
    private Status status;
    private Priority priority;
    private LocalDateTime deadline;
    private LocalDateTime createdAt;
    private String description;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserResponseDTO user;

}
