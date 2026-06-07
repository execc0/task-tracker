package org.example.task_tracker.outbox;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findOutboxEventsBySentIs(boolean sent);

    void deleteByCreatedAtBeforeAndSentIs(LocalDateTime localDateTime, boolean sent);
}
