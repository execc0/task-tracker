package org.example.task_tracker.service;

import io.jsonwebtoken.security.Password;
import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.exception.UserAlreadyExistsException;
import org.example.task_tracker.model.Role;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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

    public User updateUserEmail(String email, Long id) {
        User user = getUserById(id);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public User updateUser(User user, Long id) {
        User userToUpdate = getUserById(id);
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        return userRepository.save(userToUpdate);

    }

    public User updateUserName(String name, Long id) {
        User user = getUserById(id);
        user.setName(name);
        return userRepository.save(user);
    }

    public User updateUserRole(Role role, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с данным id не найден"));
        user.setRole(role);
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    // Всё что ниже - методы, которые вызываются с ролью USER (или ADMIN)
    public User updateOwnEmail(String email) {
        if (userRepository.findUserByEmail(email).isPresent()) { throw new UserAlreadyExistsException("Данный email уже занят"); }
        User user = getCurrentUser();
        user.setEmail(email);
        return userRepository.save(user);
    }

    public User updateOwnUsername(String username) {
        if (userRepository.findUserByUsername(username).isPresent()) { throw new UserAlreadyExistsException("Данный username уже занят"); }
        User user = getCurrentUser();
        user.setUsername(username);
        return userRepository.save(user);
    }

    public User updateOwnName(String name) {
        User user = getCurrentUser();
        user.setName(name);
        return userRepository.save(user);
    }

    public User updateOwnPassword(String password) {
        if(password.length() < 8) throw new IllegalArgumentException("Пароль должен быть не менее 8 символов");
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
