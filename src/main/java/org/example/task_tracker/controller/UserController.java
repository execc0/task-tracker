package org.example.task_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.User;
import org.example.task_tracker.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers () {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void removeUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Valid @RequestBody User user, @PathVariable Long id) {
        return userService.updateUser(user, id);
    }

    @PatchMapping("/{id}/name")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserName(@PathVariable Long id, @NotBlank(message = "Имя не может быть пустым") String name) {
        return userService.updateUserName(name, id);
    }

    @PatchMapping("/{id}/email")
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUserEmail(@PathVariable Long id, @RequestParam @Email(message = "Неверный формат email") String email) {
        return userService.updateUserEmail(email, id);
    }

    @PatchMapping("/{id}/role")
    public User updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        return userService.updateUserRole(role, id);
    }

    @PatchMapping("/me/username")
    public User updateOwnUsername (@RequestParam @NotBlank(message = "Username не может быть пустым") String username) {
        return userService.updateOwnUsername(username);
    }

    @PatchMapping("/me/email")
    public User updateOwnEmail (@RequestParam @NotBlank(message = "Email не может быть пустым") @Email(message = "Неверный формат email") String email) {
        return userService.updateOwnEmail(email);
    }

    @PatchMapping("/me/password")
    public User updateOwnPassword(@RequestParam @NotBlank(message = "Password не может быть пустым") String password) {
        return userService.updateOwnPassword(password);
    }

    @PatchMapping("/me/name")
    public User updateOwnName(@RequestParam @NotBlank(message = "Имя не может быть пустым") String name) {
        return userService.updateOwnName(name);
    }

}
