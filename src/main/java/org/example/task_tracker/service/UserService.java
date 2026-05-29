package org.example.task_tracker.service;

import org.example.task_tracker.exception.ResourceNotFoundException;
import org.example.task_tracker.model.User;
import org.example.task_tracker.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateUserName(String name, Long id) {
        User user = getUserById(id);
        user.setName(name);
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Неверно указан id пользователя"));
    }

    public User updateUserEmail(String email, Long id) {
        User user = getUserById(id);
        user.setEmail(email);
        return userRepository.save(user);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user, Long id) {
        User userToUpdate = getUserById(id);
        userToUpdate.setEmail(user.getEmail());
        userToUpdate.setName(user.getName());
        return userRepository.save(userToUpdate);

    }
}
