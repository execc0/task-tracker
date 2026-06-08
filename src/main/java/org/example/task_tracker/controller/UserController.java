package org.example.task_tracker.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.task_tracker.DTO.response.AdminUserResponseDTO;
import org.example.task_tracker.DTO.response.UserResponseDTO;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "2. Users", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminUserResponseDTO> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public AdminUserResponseDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @PatchMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserName(@PathVariable Long id, @RequestParam @NotBlank(message = "Имя не может быть пустым") String name) {
        return userService.updateUserName(name, id);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserEmail(@PathVariable Long id, @RequestParam @Email(message = "Неверный формат email") String email) {
        return userService.updateUserEmail(email, id);
    }

    @PatchMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponseDTO updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        return userService.updateUserRole(role, id);
    }

    // Всё что ниже - эндпоинты для USER (ADMIN тоже доступны).
    @PatchMapping("/me/username")
    public UserResponseDTO updateOwnUsername(@RequestParam @NotBlank(message = "Username не может быть пустым") String username) {
        return userService.updateOwnUsername(username);
    }

    @PatchMapping("/me/email")
    public UserResponseDTO updateOwnEmail(@RequestParam @NotBlank(message = "Email не может быть пустым") @Email(message = "Неверный формат email") String email) {
        return userService.updateOwnEmail(email);
    }

    @PatchMapping("/me/password")
    public UserResponseDTO updateOwnPassword(@RequestParam @NotBlank(message = "Password не может быть пустым") String password) {
        return userService.updateOwnPassword(password);
    }

    @PatchMapping("/me/name")
    public UserResponseDTO updateOwnName(@RequestParam @NotBlank(message = "Имя не может быть пустым") String name) {
        return userService.updateOwnName(name);
    }

}
