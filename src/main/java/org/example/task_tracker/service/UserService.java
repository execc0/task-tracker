package org.example.task_tracker.service;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.example.task_tracker.DTO.mapper.UserMapper;
import org.example.task_tracker.DTO.response.UserResponseDTO;
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
@Slf4j
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    // Методы, которые вызываются только с ролью ADMIN
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Неверно указан id пользователя"));
        return userMapper.toDTO(user);
    }

    public List<UserResponseDTO> getAllUsers() {
        return userMapper.toDTOList(userRepository.findAll());
    }

    @Transactional
    public UserResponseDTO updateUserEmail(@NotBlank @Email String email, Long id) {
        User currentUser = getCurrentUser();
        if (userRepository.findUserByEmail(email).isPresent()) {
            log.warn("Attempt to change email by admin to email = {} already taken", email);
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
        User user = getUserByIdInternal(id);
        String oldEmail = user.getEmail();
        user.setEmail(email);
        User saved = userRepository.save(user);
        log.info("User userId = {} updated email by admin userId = {}",
                saved.getId(), currentUser.getId());
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateUserName(@NotBlank String name, Long id) {
        User currentUser = getCurrentUser();
        User user = getUserByIdInternal(id);
        user.setName(name);
        User saved = userRepository.save(user);
        log.info("Updated name for user userId = {} by admin userId = {}, new name = {}", saved.getId(), currentUser.getId(), name);
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateUserRole(Role role, Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с данным id не найден"));
        User currentUser = getCurrentUser();
        user.setRole(role);
        User saved = userRepository.save(user);
        log.info("Updated role for user userId = {} by admin userId = {}, new role = {}", saved.getId(), currentUser.getId(), role);
        return userMapper.toDTO(saved);
    }

    @Transactional
    public void deleteUserById(Long id) {
        User user = getCurrentUser();
        userRepository.deleteById(id);
        log.info("User userId = {} deleted by admin userId = {}", id, user.getId());
    }

    // Всё что ниже - методы, которые вызываются с ролью USER (или ADMIN)
    @Transactional
    public UserResponseDTO updateOwnEmail(@NotBlank String email) {
        if (isEmailTaken(email)) {
            log.warn("Attempt to take email = {} already taken", email);
            throw new UserAlreadyExistsException("Данный email уже занят");
        }
        User user = getCurrentUser();
        String oldEmail = user.getEmail();
        user.setEmail(email);
        User saved = userRepository.save(user);
        log.info("User userId = {} updated OWN email", saved.getId());
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateOwnUsername(@NotBlank String username) {
        if (isUsernameTaken(username)) {
            log.warn("Attempt to take username = {} already taken", username);
            throw new UserAlreadyExistsException("Данный username уже занят");
        }
        User user = getCurrentUser();
        String oldUsername = user.getUsername();
        user.setUsername(username);
        User saved = userRepository.save(user);
        log.info("User userId = {} updated OWN username, old username = {} new username = {}", saved.getId(), oldUsername, user.getUsername());
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateOwnName(@NotBlank String name) {
        User user = getCurrentUser();
        String oldName = user.getName();
        user.setName(name);
        User saved = userRepository.save(user);
        log.info("User userId = {} updated OWN name, old name = {} new name = {}", saved.getId(), oldName, user.getName());
        return userMapper.toDTO(saved);
    }

    @Transactional
    public UserResponseDTO updateOwnPassword(@NotBlank String password) {
        if (password == null || password.length() < 8)
            throw new IllegalArgumentException("Пароль должен быть не менее 8 символов");
        User user = getCurrentUser();
        user.setPassword(passwordEncoder.encode(password));
        User saved = userRepository.save(user);
        log.info("User userId = {} updated OWN password", user.getId());
        return userMapper.toDTO(saved);
    }

    protected User getCurrentUser() {
        User user = (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
        return user;
    }

    protected User getUserByIdInternal(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Неверно указан id пользователя"));
    }

    protected boolean isEmailTaken(String email) {
        return userRepository.findUserByEmail(email).isPresent();
    }

    protected boolean isUsernameTaken(String username) {
        return userRepository.findUserByUsername(username).isPresent();
    }

}
