package org.example.task_tracker.DTO.mapper;

import org.example.task_tracker.DTO.response.TaskResponseDTO;
import org.example.task_tracker.model.Task;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TaskMapper {
    private final UserMapper userMapper;

    public TaskMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public TaskResponseDTO toDTO(Task task, boolean includeUser) {
        TaskResponseDTO taskResponseDTO = new TaskResponseDTO();
        taskResponseDTO.setTitle(task.getTitle());
        taskResponseDTO.setDescription(task.getDescription());
        taskResponseDTO.setStatus(task.getStatus());
        taskResponseDTO.setPriority(task.getPriority());
        taskResponseDTO.setCreatedAt(task.getCreatedAt());
        taskResponseDTO.setDeadline(task.getDeadline());
        taskResponseDTO.setId(task.getId());
        if (includeUser && task.getUser() != null) {
            taskResponseDTO.setUser(userMapper.toDTO(task.getUser()));
        }
        return taskResponseDTO;
    }

    public List<TaskResponseDTO> toDTOList(List<Task> taskList, boolean includeUser) {
        return taskList.stream()
                .map(task -> toDTO(task, includeUser))
                .toList();
    }

    public List<TaskResponseDTO> toDTOList(List<Task> taskList) {
        return toDTOList(taskList, false);
    }

    public TaskResponseDTO toDTO(Task task) {
        return toDTO(task, false);
    }
}
