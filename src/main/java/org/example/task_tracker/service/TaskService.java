package org.example.task_tracker.service;

import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.springframework.stereotype.Service;
import org.example.task_tracker.repository.TaskRepository;

import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task createTask(String title) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(Status.TODO);
        return taskRepository.save(task);
    }


    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    public Task updateTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getStatus() == Status.DONE) {
            throw new IllegalStateException("Нельзя изменить статус завершённой задачи");
        }
        if (task.getStatus() == Status.IN_PROGRESS && (status == Status.TODO)) {
            throw new IllegalStateException("Нельзя изменить статус задачи с IN PROGRESS на TODO");
        }
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        taskToUpdate.setStatus(updatedTask.getStatus());
        taskToUpdate.setTitle(updatedTask.getTitle());
        taskToUpdate.setDeadline(updatedTask.getDeadline());
        taskToUpdate.setPriority(updatedTask.getPriority());
        taskToUpdate.setUserId(updatedTask.getUserId());
        return taskRepository.save(taskToUpdate);
    }

    public List<Task> findTasksByUserId(Long id) {
        return taskRepository.findTasksByUserId(id);
    }
}
