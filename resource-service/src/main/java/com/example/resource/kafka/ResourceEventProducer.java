package com.example.resource.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ResourceEventProducer {

    private final KafkaTemplate<String, ResourceUploadedEvent> kafkaTemplate;

    public ResourceEventProducer(KafkaTemplate<String, ResourceUploadedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendResourceUploaded(Long resourceId) {
        kafkaTemplate.send(
                KafkaTopics.RESOURCE_UPLOADED,
                new ResourceUploadedEvent(resourceId)
        );
    }
}
