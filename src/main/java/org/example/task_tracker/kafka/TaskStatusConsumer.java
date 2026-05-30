package org.example.task_tracker.kafka;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskStatusConsumer {

    @KafkaListener(topics = KafkaTopics.TASK_STATUS_CHANGED, groupId = "task-tracker-group")
    public void onStatusChange(String message) {
        log.info("Получено сообщение из Kafka: {}", message);

    }
}
