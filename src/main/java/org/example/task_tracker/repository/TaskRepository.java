package org.example.task_tracker.repository;

import org.example.task_tracker.model.Status;
import org.example.task_tracker.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findTasksByUserId(long userId);

    Optional<Task> findTaskById(Long id);

    Long countTasksByUserIdAndStatusIn(long id, List<Status> statusList);

    List<Task> findTasksByUserIsNull();

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.user")
    List<Task> findAllWithUsers();

    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.user WHERE t.user.id = :userId")
    List<Task> findTasksByUserIdWithUser(@Param("userId") Long userId);

}
