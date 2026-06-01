package org.example.task_tracker.security.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.task_tracker.model.User;
import org.example.task_tracker.security.jwt.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        HashMap<String, String> response = new HashMap<>();
        User user = authService.register(request);
        String token = jwtService.generateToken(user);
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        HashMap<String, String> response = new HashMap<>();
        User user = authService.findByUsername(request.getUsername());
        authService.login(request);
        String token = jwtService.generateToken(user);
        response.put("token", token);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
