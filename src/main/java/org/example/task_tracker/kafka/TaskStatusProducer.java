package org.example.task_tracker.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskStatusProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public TaskStatusProducer(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendStatusChange(Long taskId, Long userId, String newStatus) {
        String message = String.format("{\"taskId\": %d, \"status\": \"%s\", \"userId\": %d}", taskId, newStatus, userId);
        kafkaTemplate.send(KafkaTopics.TASK_STATUS_CHANGED, message);
        log.info("Отправлено сообщение в топик {} Kafka: {}", KafkaTopics.TASK_STATUS_CHANGED, message);
    }
}
