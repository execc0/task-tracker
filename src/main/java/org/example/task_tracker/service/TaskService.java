package org.example.task_tracker.service;

import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.TaskStatusProducer;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.model.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.example.task_tracker.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
public class TaskService {
    private final TaskStatusProducer producer;
    private final TaskRepository taskRepository;

    public TaskService(TaskStatusProducer producer, TaskRepository taskRepository) {
        this.producer = producer;
        this.taskRepository = taskRepository;
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
        producer.sendStatusChange(id, task.getUser().getId(), status.name());
        return taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public List <Task> getOwnTasks() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return taskRepository.findTasksByUserId(user.getId());
    }

    public Task getOwnTask(Long id) {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        if (!(id == user.getId())) {
            throw new AccessDeniedException("Доступ запрещён");
        }
        return taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Задача с id %d не найдена", id)));
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        taskToUpdate.setStatus(updatedTask.getStatus());
        taskToUpdate.setTitle(updatedTask.getTitle());
        taskToUpdate.setDeadline(updatedTask.getDeadline());
        taskToUpdate.setPriority(updatedTask.getPriority());
        taskToUpdate.setUser(updatedTask.getUser());
        return taskRepository.save(taskToUpdate);
    }

    public List<Task> findTasksByUserId(Long id) {
        return taskRepository.findTasksByUserId(id);
    }
}
