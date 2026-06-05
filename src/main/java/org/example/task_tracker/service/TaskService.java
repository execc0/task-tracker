package org.example.task_tracker.service;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.TaskStatusProducer;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Validated
@Slf4j
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
        User user = userService.getCurrentUser();
        Task saved = taskRepository.save(task);
        log.info("Created task title = {}, taskId = {} by admin userId = {}", saved.getTitle(), saved.getId(), user.getId());
        return saved;
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
        User user = userService.getCurrentUser();
        taskRepository.deleteById(id);
        log.info("Task with id = {} deleted by admin userId = {}", id, user.getId());
    }

    @Transactional
    public Task updateTask(Long id, @Valid Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        resultTask.setUser(updatedTask.getUser());
        Task saved = taskRepository.save(resultTask);
        log.info("Updated task title = {}, taskId = {}, by admin userId = {}", saved.getTitle(), saved.getId(), user.getId());
        return saved;
    }

    @Transactional
    public Task updateTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        task.setStatus(status);
        Task saved = taskRepository.save(task);
        log.info("Updated task status title = {}, taskId = {}, new status = {} by admin userId = {}",
                saved.getTitle(), saved.getId(), saved.getStatus(), user.getId());
        return saved;
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
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Задача с id %d не найдена", id)));
        if (task.getUser().getId() != user.getId()) {
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        return task;
    }

    @Transactional
    public Task createOwnTask(@Valid Task task) {
        User user = userService.getCurrentUser();
        Long taskCount = taskRepository.countTasksByUserIdAndStatusIn(user.getId(), List.of(Status.TODO, Status.IN_PROGRESS));
        if (taskCount >= 20 && user.getRole() == Role.USER) {
            log.warn("User with userId = {} reached task limit", user.getId());
            throw new IllegalStateException("Превышен лимит задач (максимум 20)");
        }
        task.setUser(user);
        Task saved = taskRepository.save(task);
        log.info("Created new task taskId = {}, title = {} for user userId = {}", saved.getId(), saved.getTitle(), saved.getId());
        return saved;

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
        Task saved = taskRepository.save(resultTask);
        log.info("Task taskId = {} updated by user userId = {}", saved.getId(), user.getId());
        return saved;
    }

    @Transactional
    public Task updateOwnTaskStatus(Long id, Status status) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getStatus() == Status.DONE) {
            log.warn("Attempt to change status DONE of task with taskId = {}", task.getId());
            throw new IllegalStateException("Нельзя изменить статус завершённой задачи");
        }
        if (task.getStatus() == Status.IN_PROGRESS && (status == Status.TODO)) {
            log.warn("Attempt to set status IN_PROGRESS to TODO of task with taskId = {}", task.getId());
            throw new IllegalStateException("Нельзя изменить статус задачи с IN PROGRESS на TODO");
        }
        User user = userService.getCurrentUser();
        if (task.getUser().getId() != user.getId()) {
            log.warn("Attempt to change someone else's task taskId = {} by user userId = {}", task.getId(), user.getId());
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        task.setStatus(status);
        producer.sendStatusChange(id, task.getUser().getId(), status.name());
        Task saved = taskRepository.save(task);
        log.info("Task taskId = {} changed status to {} by userId = {}", saved.getId(), saved.getStatus(), user.getId());
        return saved;
    }

    @Transactional
    public Task takeAvailableTask(Long id) {
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        if (task.getUser() != null) {
            log.warn("Attempt to take a task with taskId = {} already taken by userId = {}", id, task.getUser().getId());
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        User user = userService.getCurrentUser();
        task.setUser(user);
        Task saved = taskRepository.save(task);
        log.info("User with id = {} took task with title = {}, taskId = {}", user.getId(), saved.getTitle(), saved.getId());
        return saved;
    }

    @Transactional
    public void deleteOwnTask(Long id) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача с данным id не найдена"));
        if (task.getUser() == null || task.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Задача с данным id не найдена");
        }
        taskRepository.deleteById(id);
        log.info("Task with title = {}, taskId = {} deleted by user with id = {}", task.getTitle(), id, user.getId());
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
