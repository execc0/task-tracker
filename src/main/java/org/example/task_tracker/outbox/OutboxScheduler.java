package org.example.task_tracker.outbox;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Transactional(readOnly = true)
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxScheduler(OutboxRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void readFromOutbox() {
        List<OutboxEvent> messageList = outboxRepository.findOutboxEventsBySentIs(false);
        for (OutboxEvent message : messageList) {
            kafkaTemplate.send(message.getTopic(), message.getKey(), message.getPayload());
            message.setSent(true);
        }
    }

    @Scheduled(cron = "0 0 * * * *") // Каждый час
    public void cleanOutobox() {
        outboxRepository.deleteByCreatedAtBeforeAndSentIs(LocalDateTime.now().minusDays(7), true); // Удаляем отправленные задачи старше семи дней
    }
}
