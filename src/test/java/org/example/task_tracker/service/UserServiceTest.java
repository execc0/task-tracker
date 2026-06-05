package org.example.task_tracker.service;


import org.example.task_tracker.DTO.mapper.UserMapper;
import org.example.task_tracker.exception.UserAlreadyExistsException;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    @Spy
    @InjectMocks
    private UserService userService;

    @Spy
    private UserMapper userMapper = new UserMapper();

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUsername("Test User");
        user.setName("Тестовый пользователь");
        user.setEmail("testuser@gmail.com");
        user.setPassword("123456789");
    }

    @Nested
    class UpdateOwnPassword {

        static Stream<String> provideInvalidPasswords() {
            return Stream.of("12345", "", null, "7chars_");
        }

        @Test
        void shouldUpdatePassword() {

            doReturn(user).when(userService).getCurrentUser();
            when(passwordEncoder.encode("new_password")).thenReturn("encoded_password");
            when(userRepository.save(user)).thenReturn(user);

            userService.updateOwnPassword("new_password");

            verify(passwordEncoder, times(1)).encode("new_password");

            assertEquals("encoded_password", user.getPassword());
        }

        @ParameterizedTest
        @MethodSource("provideInvalidPasswords")
        void shouldThrowExceptionWhenPasswordIsNullOrShort(String password) {

            assertThrows(IllegalArgumentException.class, () -> userService.updateOwnPassword(password));

        }

    }

    @Nested
    class updateOwnUsername {

        @Test
        void shouldUpdateOwnUsername() {
            String newUsername = "New_Username";

            doReturn(user).when(userService).getCurrentUser();
            when(userRepository.save(user)).thenReturn(user);
            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());

            assertEquals(newUsername, userService.updateOwnUsername(newUsername).getUsername());

        }

        @Test
        void shouldThrowExceptionWhenUsernameTaken() {

            User anotherUser = new User();
            anotherUser.setUsername("New_Username");

            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(anotherUser));

            assertThrows(UserAlreadyExistsException.class, () -> userService.updateOwnUsername("New_Username"));

        }

    }

    @Nested
    class updateOwnEmail {

        @Test
        void shouldUpdateOwnEmail() {
            String newEmail = "new@email.com";

            doReturn(user).when(userService).getCurrentUser();
            when(userRepository.save(user)).thenReturn(user);
            when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

            assertEquals(newEmail, userService.updateOwnEmail(newEmail).getEmail());

        }

        @Test
        void shouldThrowExceptionWhenEmailTaken() {

            User anotherUser = new User();
            anotherUser.setEmail("new@email.com");

            when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(anotherUser));

            assertThrows(UserAlreadyExistsException.class, () -> userService.updateOwnEmail("new@email.com"));

        }
    }


}
