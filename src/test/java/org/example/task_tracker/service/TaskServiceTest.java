package org.example.task_tracker.service;

import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.kafka.TaskStatusProducer;
import org.example.task_tracker.model.*;
import org.example.task_tracker.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserService userService;

    @Mock
    private TaskStatusProducer producer;

    @InjectMocks
    private TaskService taskService;


    @Nested
    class CreateOwnTask {
        private User currentUser;
        private Task task;

        @BeforeEach
        void setUp() {

            currentUser = new User();
            currentUser.setId(1L);
            currentUser.setRole(Role.USER);

            task = new Task();
            task.setTitle("Тестовая задача");
            task.setPriority(Priority.HIGH);

        }

        @ParameterizedTest
        @ValueSource(longs = {0L, 1L, 10L, 19L})
        void shouldCreateOwnTask(Long taskCount) {

            when(userService.getCurrentUser()).thenReturn(currentUser);
            when(taskRepository.countTasksByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(taskCount);
            when(taskRepository.save(task)).thenReturn(task);

            Task result = taskService.createOwnTask(task);

            assertEquals("Тестовая задача", result.getTitle());
            assertEquals(currentUser, result.getUser());
        }

        @ParameterizedTest
        @ValueSource(longs = {20L, 21L, 1000L, 3000L})
        void shouldThrowExceptionWhenTaskLimitExceeded(Long taskCount) {

            when(userService.getCurrentUser()).thenReturn(currentUser);
            when(taskRepository.countTasksByUserIdAndStatusIn(anyLong(), anyList())).thenReturn(taskCount);

            assertThrows(IllegalStateException.class, () -> taskService.createOwnTask(task));
        }
    }

    @Nested
    class TakeAvailableTask {
        private User user;

        @BeforeEach
        void setUp() {
            user = new User();
            user.setId(1L);
            user.setRole(Role.USER);
        }

        @Test
        void shouldTakeAvailableTask() {

            Task task = new Task();
            task.setTitle("Тестовая задача");
            task.setPriority(Priority.MEDIUM);
            task.setId(1L);

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));
            when(userService.getCurrentUser()).thenReturn(user);
            when(taskRepository.save(task)).thenReturn(task);

            assertEquals(task, taskService.takeAvailableTask(1L));
        }

        @Test
        void shouldThrowExceptionWhenTaskNotFound() {

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.takeAvailableTask(1L));

        }

        @Test
        void shouldThrowExceptionWhenTaskAlreadyTaken() {

            Task task = new Task();
            task.setTitle("Тестовая задача");
            task.setPriority(Priority.MEDIUM);
            task.setId(1L);
            task.setUser(user);

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));

            assertThrows(ResourceNotFoundException.class, () -> taskService.takeAvailableTask(1L));
        }

    }

    @Nested
    class UpdateOwnTaskStatus {

        private User user;
        private Task task;

        @BeforeEach
        void setUp() {

            user = new User();
            user.setId(1L);
            user.setRole(Role.USER);

            task = new Task();
            task.setTitle("Тестовая задача");
            task.setPriority(Priority.MEDIUM);
            task.setId(1L);

        }

        static Stream<Arguments> provideValidStatuses() {
            return Stream.of(
                    Arguments.of(Status.TODO, Status.IN_PROGRESS),
                    Arguments.of(Status.TODO, Status.DONE),
                    Arguments.of(Status.IN_PROGRESS, Status.DONE)
            );
        }

        static Stream<Arguments> provideInvalidStatuses() {
            return Stream.of(
                    Arguments.of(Status.IN_PROGRESS, Status.TODO),
                    Arguments.of(Status.DONE, Status.IN_PROGRESS),
                    Arguments.of(Status.DONE, Status.TODO)
            );
        }

        @ParameterizedTest
        @MethodSource("provideValidStatuses")
        public void shouldUpdateOwnTaskStatus(Status oldStatus, Status newStatus) {


            task.setStatus(oldStatus);
            task.setUser(user);


            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));
            when(userService.getCurrentUser()).thenReturn(user);
            when(taskRepository.save(task)).thenReturn(task);

            Task updatedTask = taskService.updateOwnTaskStatus(1L, newStatus);

            assertEquals(newStatus, updatedTask.getStatus());
        }

        @ParameterizedTest
        @MethodSource("provideValidStatuses")
        public void shouldThrowExceptionWhenTaskNotFound(Status oldStatus, Status newStatus) {

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.updateOwnTaskStatus(1L, newStatus));
        }

        @ParameterizedTest
        @MethodSource("provideInvalidStatuses")
        public void shouldThrowExceptionWhenStatusChangeIsInvalid(Status oldStatus, Status newStatus) {

            task.setStatus(oldStatus);

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));

            assertThrows(IllegalStateException.class, () -> taskService.updateOwnTaskStatus(1L, newStatus));

        }


        @ParameterizedTest
        @MethodSource("provideValidStatuses")
        public void shouldThrowExceptionWhenUpdatingNotOwnTaskStatus(Status oldStatus, Status newStatus) {
            User anotherUser = new User();
            user.setName("Другой юзер");
            user.setId(2L);

            task.setStatus(oldStatus);
            task.setUser(anotherUser);

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));
            when(userService.getCurrentUser()).thenReturn(user);

            assertThrows(IllegalStateException.class, () -> taskService.updateOwnTaskStatus(1L, newStatus));

        }

    }

    @Nested
    class UpdateOwnTask {

        private Task task;
        private User user;
        private Task updatedTask;

        @BeforeEach
        void setUp() {

            user = new User();
            user.setName("Тестовый пользователь");
            user.setId(1L);

            task = new Task();
            task.setTitle("Тестовая задача");
            task.setStatus(Status.TODO);
            task.setPriority(Priority.LOW);
            task.setId(1L);

            updatedTask = new Task();
            updatedTask.setTitle("Новая задача");
            updatedTask.setStatus(Status.IN_PROGRESS);
            updatedTask.setPriority(Priority.HIGH);

        }

        @Test
        public void shouldUpdateOwnTask() {

            task.setUser(user);

            when(userService.getCurrentUser()).thenReturn(user);
            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));
            when(taskRepository.save(task)).thenReturn(task);

            Task resultTask = taskService.updateOwnTask(1L, updatedTask);

            assertEquals(updatedTask.getTitle(), resultTask.getTitle());
            assertEquals(updatedTask.getStatus(), resultTask.getStatus());
            assertEquals(updatedTask.getPriority(), resultTask.getPriority());
            assertEquals(task.getId(), resultTask.getId());

        }

        @Test
        public void shouldThrowExceptionWhenTaskNotFound() {

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> taskService.updateOwnTask(1L, updatedTask));
        }

        @Test
        public void shouldThrowExceptionWhenTaskUserIsNull() {

            when(taskRepository.findTaskById(1L)).thenReturn(Optional.of(task));

            assertThrows(ResourceNotFoundException.class, () -> taskService.updateOwnTask(1L, updatedTask));

        }

        @Test
        public void shouldThrowExceptionWhenIdsDontMatch() {

            task.setUser(user);

            User anotherUser = new User();
            anotherUser.setId(2);

            when(taskRepository.findTaskById(anyLong())).thenReturn(Optional.of(task));
            when(userService.getCurrentUser()).thenReturn(anotherUser);

            assertThrows(ResourceNotFoundException.class, () -> taskService.updateOwnTask(1L, updatedTask));


        }

    }


}