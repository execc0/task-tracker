package org.example.task_tracker.repository;

import jakarta.validation.constraints.NotBlank;
import org.example.task_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);

    Optional<Object> findUserByEmail(String username);
}