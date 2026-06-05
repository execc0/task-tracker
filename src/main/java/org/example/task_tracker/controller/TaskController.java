package org.example.task_tracker.controller;

import jakarta.validation.Valid;
import org.example.task_tracker.DTO.response.TaskResponseDTO;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.service.TaskService;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponseDTO getTaskById(@PathVariable Long id) {
        return taskService.getTaskById(id);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<TaskResponseDTO> getTasksByUserId(@PathVariable Long userId) {
        return taskService.findTasksByUserId(userId);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponseDTO createTask(@Valid @RequestBody Task task) {
        return taskService.createTask(task);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponseDTO updateTask(@PathVariable Long id, @Valid @RequestBody Task updatedTask) {
        return taskService.updateTask(id, updatedTask);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public TaskResponseDTO updateTaskStatus(@PathVariable Long id, @RequestParam Status status) {
        return taskService.updateTaskStatus(id, status);
    }

    // Всё что ниже - эндпоинты для USER (для ADMIN тоже доступны).
    @GetMapping("/available")
    public List<TaskResponseDTO> getAvailableTasks() {
        return taskService.getAvailableTasks();
    }

    @PostMapping("/available/{id}")
    public TaskResponseDTO takeAvailableTask(@PathVariable Long id) {
        return taskService.takeAvailableTask(id);
    }

    @GetMapping("/my")
    public List<TaskResponseDTO> getOwnTasks() {
        return taskService.getOwnTasks();
    }

    @GetMapping("/my/{id}")
    public TaskResponseDTO getOwnTask(@PathVariable Long id) {
        return taskService.getOwnTask(id);
    }

    @PostMapping("/my")
    public TaskResponseDTO createOwnTask(@Valid @RequestBody Task task) {
        return taskService.createOwnTask(task);
    }

    @PutMapping("/my/{id}")
    public TaskResponseDTO updateOwnTask(@Valid @RequestBody Task updatedTask, @PathVariable Long id) {
        return taskService.updateOwnTask(id, updatedTask);
    }

    @PatchMapping("/my/{id}/status")
    public TaskResponseDTO updateOwnTaskStatus(@PathVariable Long id, @RequestParam Status status) {
        return taskService.updateOwnTaskStatus(id, status);
    }

    @DeleteMapping("/my/{id}")
    public void deleteOwnTask(@PathVariable Long id) {
        taskService.deleteOwnTask(id);
    }

}
