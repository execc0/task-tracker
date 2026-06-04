package org.example.task_tracker.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.exception.UserAlreadyExistsException;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Validated
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // Методы, которые вызываются только с ролью ADMIN
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Неверно указан id пользователя"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateUserEmail(@NotBlank @Email String email, Long id) {
        User user = getUserById(id);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUser(@Valid User user, Long id) {
        User userToUpdate = getUserById(id);
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        return userRepository.save(userToUpdate);

    }

    @Transactional
    public User updateUserName(@NotBlank String name, Long id) {
        User user = getUserById(id);
        user.setName(name);
        return userRepository.save(user);
    }

    @Transactional
    public User updateUserRole(Role role, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с данным id не найден"));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    // Всё что ниже - методы, которые вызываются с ролью USER (или ADMIN)
    @Transactional
    public User updateOwnEmail(@NotBlank String email) {
        if (userRepository.findUserByEmail(email).isPresent()) {
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
        User user = getCurrentUser();
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public User updateOwnUsername(@NotBlank String username) {
        if (userRepository.findUserByUsername(username).isPresent()) {
            throw new UserAlreadyExistsException("Данный username уже занят");
        }
        User user = getCurrentUser();
        user.setUsername(username);
        return userRepository.save(user);
    }

    @Transactional
    public User updateOwnName(@NotBlank String name) {
        User user = getCurrentUser();
        user.setName(name);
        return userRepository.save(user);
    }

    @Transactional
    public User updateOwnPassword(@NotBlank String password) {
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Пароль должен быть не менее 8 символов");
        User user = getCurrentUser();
        user.setPassword(passwordEncoder.encode(password));
        return userRepository.save(user);
    }

    protected User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return user;
    }


}
