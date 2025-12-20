package org.example.kafka;

import org.example.service.ProcessingService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ResourceUploadedListener {

    private final ProcessingService processingService;

    public ResourceUploadedListener(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "resource-uploaded",
            groupId = "resource-processor-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handle(ResourceUploadedEvent event) {
        processingService.process(event.resourceId());
    }
}

