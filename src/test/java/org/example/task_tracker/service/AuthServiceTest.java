package org.example.task_tracker.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.task_tracker.exception.UserAlreadyExistsException;
import org.example.task_tracker.model.User;
import org.example.task_tracker.outbox.OutboxRepository;
import org.example.task_tracker.repository.UserRepository;
import org.example.task_tracker.security.auth.AuthService;
import org.example.task_tracker.security.auth.RegisterRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OutboxRepository outboxRepository;

    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private AuthService authService;


    @Nested
    class Register {

        private RegisterRequest request;

        private static Stream<String> provideValidPasswords() {
            return Stream.of("12345678", "123456789", "aPasswordThatIsLongEnough");
        }

        private static Stream<String> provideInvalidPasswords() {
            return Stream.of("1", null, "1234567", "sevench");
        }

        @BeforeEach
        void setUp() {
            request = new RegisterRequest();
            request.setEmail("newemail@gmail.com");
            request.setName("User");
            request.setUsername("NewUsername");
            request.setPassword("A_Valid_Password");
        }

        @ParameterizedTest
        @MethodSource("provideValidPasswords")
        public void shouldRegisterUser(String password) {

            request.setPassword(password);

            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

            User registeredUser = authService.register(request);

            assertEquals(request.getName(), registeredUser.getName());
            assertEquals(request.getUsername(), registeredUser.getUsername());
            assertEquals(request.getEmail(), registeredUser.getEmail());
            assertEquals("encoded_password", registeredUser.getPassword());
        }


        @ParameterizedTest
        @MethodSource("provideInvalidPasswords")
        public void shouldThrowExceptionWhenPasswordIsShort(String password) {
            request.setPassword(password);

            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.empty());

            assertThrows(IllegalStateException.class, () -> authService.register(request));
        }

        @Test
        public void shouldThrowExceptionWhenUsernameTaken() {

            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.of(new User()));

            assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        }

        @Test
        public void shouldThrowExceptionWhenEmailTaken() {

            when(userRepository.findUserByUsername(anyString())).thenReturn(Optional.empty());
            when(userRepository.findUserByEmail(anyString())).thenReturn(Optional.of(new User()));

            assertThrows(UserAlreadyExistsException.class, () -> authService.register(request));
        }

    }

}
