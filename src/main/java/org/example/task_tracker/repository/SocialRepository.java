package org.example.task_tracker.repository;

import org.example.task_tracker.model.Social;
import org.example.task_tracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialRepository extends JpaRepository<Social, Long> {

    List<Social> findByUser(User user);

    Optional<Social> findByProviderAndProviderId(String provider, String providerId);

}
