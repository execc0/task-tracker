package org.example.task_tracker.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.DTO.mapper.TaskMapper;
import org.example.task_tracker.DTO.response.TaskResponseDTO;
import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.KafkaTopics;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.example.task_tracker.model.User;
import org.example.task_tracker.outbox.OutboxEvent;
import org.example.task_tracker.outbox.OutboxRepository;
import org.example.task_tracker.outbox.TaskStatusPayload;
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

    private final OutboxRepository outboxRepository;
    private final TaskMapper taskMapper;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public TaskService(OutboxRepository outboxRepository, TaskMapper taskMapper, TaskRepository taskRepository, UserService userService, ObjectMapper objectMapper) {
        this.outboxRepository = outboxRepository;
        this.taskMapper = taskMapper;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.objectMapper = objectMapper;
    }


    // Методы, которые вызываются только с ролью ADMIN
    @Transactional
    public TaskResponseDTO createTask(@Valid Task task) {
        User user = userService.getCurrentUser();
        Task saved = taskRepository.save(task);
        log.info("Created task title = {}, taskId = {} by admin userId = {}", saved.getTitle(), saved.getId(), user.getId());
        return taskMapper.toDTO(saved);
    }

    public TaskResponseDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        return taskMapper.toDTO(task);
    }

    public List<TaskResponseDTO> getAllTasks() {
        List<Task> taskList = taskRepository.findAllWithUsers();
        return taskMapper.toDTOList(taskList);
    }

    @Transactional
    public void deleteTask(Long id) {
        User user = userService.getCurrentUser();
        taskRepository.deleteById(id);
        log.info("Task with id = {} deleted by admin userId = {}", id, user.getId());
    }

    @Transactional
    public TaskResponseDTO updateTask(Long id, @Valid Task updatedTask) {
        Task taskToUpdate = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        resultTask.setUser(updatedTask.getUser());
        Task saved = taskRepository.save(resultTask);
        log.info("Updated task title = {}, taskId = {}, by admin userId = {}", saved.getTitle(), saved.getId(), user.getId());
        return taskMapper.toDTO(saved);
    }

    @Transactional
    public TaskResponseDTO updateTaskStatus(Long id, Status status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        task.setStatus(status);
        Task saved = taskRepository.save(task);
        log.info("Updated task status title = {}, taskId = {}, new status = {} by admin userId = {}",
                saved.getTitle(), saved.getId(), saved.getStatus(), user.getId());
        return taskMapper.toDTO(saved);
    }

    public List<TaskResponseDTO> findTasksByUserId(Long id) {
        return taskMapper.toDTOList(taskRepository.findTasksByUserIdWithUser(id));
    }


    // Всё что ниже - методы, которые вызываются с ролью USER (или ADMIN)
    public List<TaskResponseDTO> getAvailableTasks() {
        return taskMapper.toDTOList(taskRepository.findTasksByUserIsNull());
    }

    public List<TaskResponseDTO> getOwnTasks() {
        User user = userService.getCurrentUser();
        return taskMapper.toDTOList(taskRepository.findTasksByUserIdWithUser(user.getId()));
    }

    public TaskResponseDTO getOwnTask(Long id) {
        User user = userService.getCurrentUser();
        Task task = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Задача с id %d не найдена", id)));
        if (task.getUser().getId() != user.getId()) {
            throw new IllegalStateException("Задача с данным id вам не принадлежит");
        }
        return taskMapper.toDTO(task);
    }

    @Transactional
    public TaskResponseDTO createOwnTask(@Valid Task task) {
        User user = userService.getCurrentUser();
        Long taskCount = taskRepository.countTasksByUserIdAndStatusIn(user.getId(), List.of(Status.TODO, Status.IN_PROGRESS));
        if (taskCount >= 20 && user.getRole() == Role.USER) {
            log.warn("User with userId = {} reached task limit", user.getId());
            throw new IllegalStateException("Превышен лимит задач (максимум 20)");
        }
        task.setUser(user);
        Task saved = taskRepository.save(task);
        log.info("Created new task taskId = {}, title = {} for user userId = {}", saved.getId(), saved.getTitle(), saved.getId());
        return taskMapper.toDTO(saved);

    }

    @Transactional
    public TaskResponseDTO updateOwnTask(Long id, @Valid Task updatedTask) {
        Task taskToUpdate = taskRepository.findTaskById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Задача не найдена - указан неверный id"));
        User user = userService.getCurrentUser();
        if (taskToUpdate.getUser() == null || taskToUpdate.getUser().getId() != user.getId()) {
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        Task resultTask = updateTaskFields(taskToUpdate, updatedTask);
        Task saved = taskRepository.save(resultTask);
        log.info("Task taskId = {} updated by user userId = {}", saved.getId(), user.getId());
        return taskMapper.toDTO(saved);
    }

    @Transactional
    public TaskResponseDTO updateOwnTaskStatus(Long id, Status status) {
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
            throw new ResourceNotFoundException("Задача не найдена - указан неверный id");
        }
        task.setStatus(status);
        Task saved = taskRepository.save(task);
        TaskStatusPayload payload = new TaskStatusPayload(saved.getId(), saved.getStatus().toString(), user.getId());
        String payloadJson = toJson(payload);
        OutboxEvent outboxEvent = new OutboxEvent(KafkaTopics.TASK_STATUS_CHANGED, Long.toString(saved.getId()), payloadJson);
        outboxRepository.save(outboxEvent);
        log.info("Task taskId = {} changed status to {} by userId = {}", saved.getId(), saved.getStatus(), user.getId());
        return taskMapper.toDTO(saved);
    }

    @Transactional
    public TaskResponseDTO takeAvailableTask(Long id) {
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
        return taskMapper.toDTO(saved);
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

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации", e);
        }
    }

}
