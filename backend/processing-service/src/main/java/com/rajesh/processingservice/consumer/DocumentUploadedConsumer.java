package com.rajesh.processingservice.consumer;

import com.rajesh.processingservice.dto.DocumentUploadedEvent;
import com.rajesh.processingservice.service.OcrOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentUploadedConsumer {

    private final OcrOrchestrationService ocrOrchestrationService;

    @KafkaListener(
        topics = "document.uploaded",
        groupId = "processing-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            @Payload DocumentUploadedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received document.uploaded event - documentId: {}, partition: {}, offset: {}",
                event.getDocumentId(), partition, offset);

        try {
            ocrOrchestrationService.processDocument(event);
        } catch (Exception e) {
            log.error("Error processing document event for documentId: {}", event.getDocumentId(), e);
        }
    }
}
