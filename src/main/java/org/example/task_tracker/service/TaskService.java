package org.example.task_tracker.service;

import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.TaskStatusProducer;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.example.task_tracker.repository.TaskRepository;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
public class TaskService {
    private final TaskStatusProducer producer;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    public TaskService(TaskStatusProducer producer, TaskRepository taskRepository, UserService userService, UserRepository userRepository) {
        this.producer = producer;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.userRepository = userRepository;
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

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public Task updateTask(Long id, Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        resultTask.setUser(updatedTask.getUser());
        return taskRepository.save(resultTask);
    }

    public Task updateTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public List<Task> findTasksByUserId(Long id) {
        return taskRepository.findTasksByUserId(id);
    }

    public List<Task> getAvailableTasks() {
        return taskRepository.findTasksByUserIsNull();
    }

    public List <Task> getOwnTasks() {
        User user = userService.getCurrentUser();
        return taskRepository.findTasksByUserId(user.getId());
    }

    public Task getOwnTask(Long id) {
        User user = userService.getCurrentUser();
        if (id != user.getId()) {
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        return taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Задача с id %d не найдена", id)));
    }

    public Task createOwnTask(Task task) {
        User user = userService.getCurrentUser();
        Long taskCount = taskRepository.countTasksByUserIdAndStatusIn(user.getId(), List.of(Status.TODO, Status.IN_PROGRESS));
        if (taskCount >= 20 && user.getRole() == Role.USER) {
            throw new IllegalStateException("Превышен лимит задач (максимум 20)");
        }
        task.setUser(user);
        return taskRepository.save(task);
    }

    public Task updateOwnTask(Task updatedTask, Long id) {
        User user = userService.getCurrentUser();
        Task taskToUpdate = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (taskToUpdate.getUser() == null || taskToUpdate.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        return taskRepository.save(resultTask);
    }

    public Task updateOwnTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getStatus() == Status.DONE) {
            throw new IllegalStateException("Нельзя изменить статус завершённой задачи");
        }
        if (task.getStatus() == Status.IN_PROGRESS && (status == Status.TODO)) {
            throw new IllegalStateException("Нельзя изменить статус задачи с IN PROGRESS на TODO");
        }
        User user = userService.getCurrentUser();
        if(task.getUser().getId() != user.getId()) {
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        task.setStatus(status);
        producer.sendStatusChange(id, task.getUser().getId(), status.name());
        return taskRepository.save(task);
    }

    public Task takeAvailableTask(Long id) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getUser() != null) {
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        User user = userService.getCurrentUser();
        task.setUser(user);
        return taskRepository.save(task);
    }

    private Task updateTaskFields(Task taskToUpdate, Task updatedTask) {
        taskToUpdate.setTitle(updatedTask.getTitle());
        taskToUpdate.setPriority(updatedTask.getPriority());
        taskToUpdate.setStatus(updatedTask.getStatus());
        taskToUpdate.setDeadline(updatedTask.getDeadline());
        taskToUpdate.setDescription(updatedTask.getDescription());
        return taskToUpdate;
    }

}
