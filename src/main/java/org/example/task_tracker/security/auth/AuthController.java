package org.example.task_tracker.security.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.task_tracker.model.User;
import org.example.task_tracker.security.DTO.*;
import org.example.task_tracker.security.jwt.JwtService;
import org.example.task_tracker.service.SocialService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;
    private final SocialService socialService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @CacheEvict(value = "users", allEntries = true)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        User user = authService.findByUsername(request.getUsername());
        authService.login(request);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }

    @PostMapping("/link-social")
    @ResponseStatus(HttpStatus.OK)
    public void linkSocial(@Valid @RequestBody LinkSocialRequest request) {
        socialService.linkSocial(request);
    }

    @PostMapping("/login/telegram")
    public AuthResponse loginTelegram(@Valid @RequestBody LoginRequestTelegram request) {
        authService.loginTelegram(request);
        User user = authService.findByProvider("Telegram", request.getChatId());
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
