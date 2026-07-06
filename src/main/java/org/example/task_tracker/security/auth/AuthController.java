package org.example.task_tracker.security.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.task_tracker.model.User;
import org.example.task_tracker.security.DTO.AuthResponse;
import org.example.task_tracker.security.DTO.LoginRequest;
import org.example.task_tracker.security.DTO.RegisterRequest;
import org.example.task_tracker.security.DTO.social.UnlinkSocialRequest;
import org.example.task_tracker.security.DTO.social.signable.LoginAndLinkRequest;
import org.example.task_tracker.security.DTO.social.signable.LoginRequestTelegram;
import org.example.task_tracker.security.DTO.social.signable.RegisterAndLinkRequest;
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

    @PostMapping("/login-and-link")
    public AuthResponse loginAndLink(@Valid @RequestBody LoginAndLinkRequest request) {
        return authService.loginAndLink(request);
    }

    @PostMapping("/register-and-link")
    public AuthResponse registerAndLink(@Valid @RequestBody RegisterAndLinkRequest request) {
        return authService.registerAndLink(request);
    }

    @PostMapping("/unlink-social")
    @ResponseStatus(HttpStatus.OK)
    public void unlinkSocial(@Valid @RequestBody UnlinkSocialRequest request) {
        LoginRequest loginRequest = new LoginRequest(request.getUsername(), request.getPassword());
        authService.login(loginRequest);
        socialService.unlinkSocial(request);
    }

    @PostMapping("/login/telegram")
    public AuthResponse loginTelegram(@Valid @RequestBody LoginRequestTelegram request) {
        User user = authService.loginTelegram(request);
        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
