package org.example.task_tracker.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.task_tracker.model.User;
import org.example.task_tracker.service.UserService;
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
    public List<User> getAllUsers () {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void removeUserById(@PathVariable Long id) {
        userService.deleteUserById(id);
    }

    @PostMapping()
    public User addUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping("/{id}")
    public User updateUser(@Valid @RequestBody User user, @PathVariable Long id) {
        return userService.updateUser(user, id);
    }

    @PatchMapping("/{id}/name")
    public User updateUserName(@PathVariable Long id, @NotBlank(message = "Имя не может быть пустым") String name) {
        return userService.updateUserName(name, id);
    }

    @PatchMapping("/{id}/email")
    public User updateUserEmail(@PathVariable Long id, @RequestParam @Email(message = "Неверный формат email") String email) {
        return userService.updateUserEmail(email, id);
    }

}
