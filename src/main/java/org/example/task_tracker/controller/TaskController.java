package org.example.task_tracker.controller;

import jakarta.validation.Valid;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<Task> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUserId(@PathVariable Long userId) {
        return taskService.findTasksByUserId(userId);
    }

    @PostMapping
    public Task createTask(@Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task updatedTask) {
        return taskService.updateTask(id , updatedTask);
    }
    @PatchMapping("/{id}/status")
    public Task updateTaskStatus(@PathVariable Long id, @RequestParam Status status) {
        return taskService.updateTaskStatus(id, status);
    }
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }


}
