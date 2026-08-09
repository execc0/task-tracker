package org.example.task_tracker.repository;

import org.example.task_tracker.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findUserByUsername(String username);

    Page<User> findAll(Pageable pageable);

    Optional<User> findUserByEmail(String username);

    @Query("SELECT s.user FROM Social s WHERE s.provider=:provider AND s.providerId=:providerId")
    Optional<User> findUserByProvider(String provider, String providerId);


}