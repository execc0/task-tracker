package org.example.task_tracker.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskStatusConsumer {

    @KafkaListener(topics = KafkaTopics.TASK_STATUS_CHANGED, groupId = "task-tracker-group-test")
    public void onStatusChange(String message) {
        try {
            log.info("Получено сообщение из Kafka: {}", message);
            processMessage(message);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщение из Kafka: {}, сообщение: {}", message, e.getMessage());
            throw e;
        }
    }

    private void processMessage(String message) {
        log.info("Сообщение успешно обработно: {}", message);
    }
}
