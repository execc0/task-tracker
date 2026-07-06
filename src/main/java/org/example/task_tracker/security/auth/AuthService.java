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
import org.example.task_tracker.repository.SocialRepository;
import org.example.task_tracker.repository.UserRepository;
import org.example.task_tracker.security.DTO.AuthResponse;
import org.example.task_tracker.security.DTO.LoginRequest;
import org.example.task_tracker.security.DTO.RegisterRequest;
import org.example.task_tracker.security.DTO.social.signable.*;
import org.example.task_tracker.security.jwt.JwtService;
import org.example.task_tracker.security.telegram.HmacSignatureService;
import org.example.task_tracker.service.SocialService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
@Slf4j
@Tag(name = "0. Auth", description = "Аутентификация")
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final ObjectMapper objectMapper;
    private final HmacSignatureService hmacSignatureService;
    private final OutboxRepository outboxRepository;
    private final SocialService socialService;
    private final JwtService jwtService;
    private final SocialRepository socialRepository;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, ObjectMapper objectMapper, HmacSignatureService hmacSignatureService, OutboxRepository outboxRepository, SocialService socialService, JwtService jwtService, SocialRepository socialRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.objectMapper = objectMapper;
        this.hmacSignatureService = hmacSignatureService;
        this.outboxRepository = outboxRepository;
        this.socialService = socialService;
        this.jwtService = jwtService;
        this.socialRepository = socialRepository;
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
        Authentication auth = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(auth);
        log.info("User logged in username = {}", request.getUsername());
    }

    @Transactional
    public AuthResponse loginAndLink(LoginAndLinkRequest request) {

        LinkRequest linkRequest = request.getLinkRequest();

        verifyHMAC(linkRequest);

        if (socialRepository.findByProviderAndProviderId(linkRequest.getProvider().toLowerCase(Locale.ROOT),
                linkRequest.getProviderId()).isPresent()) {
            throw new UserAlreadyExistsException("Вы уже зарегистрированы, сначала отвяжите аккаунт командой /unlink");
        }

        login(request.getLoginRequest());
        socialService.linkSocial(linkRequest);

        User user = findByUsername(request.getLoginRequest().getUsername());
        return new AuthResponse(jwtService.generateToken(user));

    }

    @Transactional
    public AuthResponse registerAndLink(RegisterAndLinkRequest request) {

        LinkRequest linkRequest = request.getLinkRequest();

        verifyHMAC(linkRequest);

        if (socialRepository.findByProviderAndProviderId(linkRequest.getProvider().toLowerCase(Locale.ROOT),
                linkRequest.getProviderId()).isPresent()) {
            throw new UserAlreadyExistsException("Вы уже зарегистрированы, сначала отвяжите аккаунт командой /unlink");
        }

        User user = register(request.getRegisterRequest());
        Authentication auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        socialService.linkSocial(linkRequest);

        return new AuthResponse(jwtService.generateToken(user));

    }

    public User loginTelegram(LoginRequestTelegram request) {
        verifyHMAC(request);
        return findByProvider("telegram", request.getChatId());
    }

    public User findByUsername(String username) {
        return userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь с данным username не найден"));
    }

    public User findByProvider(String provider, String providerId) {
        return userRepository.findUserByProvider(provider, providerId)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден - необходима авторизация"));
    }

    private String toJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации", e);
        }
    }

    private void verifyHMAC(Signable signable) {

        if (!hmacSignatureService.signatureIsValid(signable)) {
            log.error("Подпись HMAC не совпала при проверке: {}", signable);
            throw new AuthorizationDeniedException("Не удалось авторизовать пользователя");
        }

    }

}
