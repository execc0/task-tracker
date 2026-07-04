package org.example.task_tracker.security.auth;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.exception.UserAlreadyExistsException;
import org.example.task_tracker.kafka.KafkaTopics;
import org.example.task_tracker.model.User;
import org.example.task_tracker.outbox.OutboxEvent;
import org.example.task_tracker.outbox.OutboxRepository;
import org.example.task_tracker.outbox.payload.UserPayload;
import org.example.task_tracker.repository.UserRepository;
import org.example.task_tracker.security.DTO.LoginRequest;
import org.example.task_tracker.security.DTO.LoginRequestTelegram;
import org.example.task_tracker.security.DTO.RegisterRequest;
import org.example.task_tracker.security.telegram.TelegramSecurityService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@Slf4j
@Tag(name = "0. Auth", description = "Аутентификация")
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final TelegramSecurityService telegramSecurityService;
    private final OutboxRepository outboxRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, ObjectMapper objectMapper, TelegramSecurityService telegramSecurityService, OutboxRepository outboxRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.telegramSecurityService = telegramSecurityService;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с данным username уже зарегистрирован!");
        }
        if (userRepository.findUserByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с данным email уже зарегистрирован!");
        }
        if (request.getPassword() == null || request.getPassword().length() < 8)
            throw new IllegalStateException("Пароль должен быть не менее 8 символов");
        User user = new User(request.getName(), request.getEmail(), request.getUsername(), passwordEncoder.encode(request.getPassword()));
        User saved = userRepository.save(user);
        UserPayload payload = new UserPayload(user.getUsername(), user.getName(), user.getEmail());
        String jsonPayload = toJson(payload);
        OutboxEvent outboxEvent = new OutboxEvent(KafkaTopics.USER_REGISTERED, user.getUsername(), jsonPayload);
        outboxRepository.save(outboxEvent);
        log.info("New user registered, username = {}, name = {}",
                saved.getUsername(), saved.getName());
        return saved;
    }

    public void login(LoginRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        log.info("User logged in username = {}", request.getUsername());
    }

    public void loginTelegram(LoginRequestTelegram request) {
        if (!telegramSecurityService.signatureIsValid(request)) {
            log.error("Подпись HMAC не совпала при проверке: {}", request);
            throw new AuthorizationDeniedException("Не удалось авторизовать пользователя");
        }
    }

    public User findByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с данным username не найден"));
    }

    public User findByProvider(String provider, String providerId) {
        return userRepository.findUserByProvider(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с данным chatId не найден - необходима аутентификация"));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации", e);
        }
    }

}
