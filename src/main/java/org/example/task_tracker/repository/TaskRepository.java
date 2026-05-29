package org.example.task_tracker.repository;

import org.example.task_tracker.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long>{
    List<Task> findTasksByUserId(long userId);
}
