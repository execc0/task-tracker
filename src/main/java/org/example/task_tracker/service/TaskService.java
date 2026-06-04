package org.example.task_tracker.service;

import jakarta.validation.Valid;
import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.TaskStatusProducer;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TaskService {
    private final TaskStatusProducer producer;
    private final TaskRepository taskRepository;
    private final UserService userService;

    public TaskService(TaskStatusProducer producer, TaskRepository taskRepository, UserService userService) {
        this.producer = producer;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }


    // Методы, которые вызываются только с ролью ADMIN
    @Transactional
    public Task createTask(@Valid Task task) {
        return taskRepository.save(task);
    }

    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional
    public Task updateTask(Long id, @Valid Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        resultTask.setUser(updatedTask.getUser());
        return taskRepository.save(resultTask);
    }

    @Transactional
    public Task updateTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        task.setStatus(status);
        return taskRepository.save(task);
    }

    public List<Task> findTasksByUserId(Long id) {
        return taskRepository.findTasksByUserId(id);
    }


    // Всё что ниже - методы, которые вызываются с ролью USER (или ADMIN)
    public List<Task> getAvailableTasks() {
        return taskRepository.findTasksByUserIsNull();
    }

    public List<Task> getOwnTasks() {
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

    @Transactional
    public Task createOwnTask(@Valid Task task) {
        User user = userService.getCurrentUser();
        Long taskCount = taskRepository.countTasksByUserIdAndStatusIn(user.getId(), List.of(Status.TODO, Status.IN_PROGRESS));
        if (taskCount >= 20 && user.getRole() == Role.USER) {
            throw new IllegalStateException("Превышен лимит задач (максимум 20)");
        }
        task.setUser(user);
        return taskRepository.save(task);
    }

    @Transactional
    public Task updateOwnTask(Long id, @Valid Task updatedTask) {
        Task taskToUpdate = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        if (taskToUpdate.getUser() == null || taskToUpdate.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        return taskRepository.save(resultTask);
    }

    @Transactional
    public Task updateOwnTaskStatus(Long id, Status status) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getStatus() == Status.DONE) {
            throw new IllegalStateException("Нельзя изменить статус завершённой задачи");
        }
        if (task.getStatus() == Status.IN_PROGRESS && (status == Status.TODO)) {
            throw new IllegalStateException("Нельзя изменить статус задачи с IN PROGRESS на TODO");
        }
        User user = userService.getCurrentUser();
        if (task.getUser().getId() != user.getId()) {
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        task.setStatus(status);
        producer.sendStatusChange(id, task.getUser().getId(), status.name());
        return taskRepository.save(task);
    }

    @Transactional
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

    protected Task updateTaskFields(Task taskToUpdate, Task updatedTask) {
        taskToUpdate.setTitle(updatedTask.getTitle());
        taskToUpdate.setPriority(updatedTask.getPriority());
        taskToUpdate.setStatus(updatedTask.getStatus());
        taskToUpdate.setDeadline(updatedTask.getDeadline());
        taskToUpdate.setDescription(updatedTask.getDescription());
        return taskToUpdate;
    }

}
