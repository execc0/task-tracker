package org.example.task_tracker.security.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.task_tracker.model.User;
import org.example.task_tracker.security.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        HashMap<String, String> response = new HashMap<>();
        User user = authService.register(request);
        String token = jwtService.generateToken(user);
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        HashMap<String, Object> response = new HashMap<>();
        User user = authService.findByUsername(request.getUsername());
        authService.login(request);
        String token = jwtService.generateToken(user);
        response.put("token", token);
        response.put("id", user.getId());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
