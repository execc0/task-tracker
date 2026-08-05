package org.example.task_tracker.outbox;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@Profile("prod")
@Slf4j
public class OutboxSchedulerProd {

    private final OutboxRepository outboxRepository;

    public OutboxSchedulerProd(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void relayEvents() {
        List<OutboxEvent> events = outboxRepository.findAll();
        if (!events.isEmpty()) {
            outboxRepository.deleteAll(events);
            log.info("Cleared {} outbox events (Kafka delivery disabled in prod)", events.size());
        }
    }
}