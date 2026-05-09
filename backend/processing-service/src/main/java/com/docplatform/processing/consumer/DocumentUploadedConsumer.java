package com.docplatform.processing.consumer;

import com.docplatform.processing.dto.DocumentUploadedEvent;
import com.docplatform.processing.service.OcrOrchestrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentUploadedConsumer {

    private final OcrOrchestrationService orchestrationService;

    @KafkaListener(
        topics = "${kafka.topics.document-uploaded:document.uploaded}",
        groupId = "${spring.kafka.consumer.group-id:processing-service-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void onDocumentUploaded(
            @Payload DocumentUploadedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        log.info("Received document.uploaded event: documentId={} partition={} offset={}",
                event.getDocumentId(), partition, offset);

        try {
            orchestrationService.processDocument(event);
        } catch (Exception e) {
            log.error("Failed to process documentId={}: {}", event.getDocumentId(), e.getMessage(), e);
            orchestrationService.handleProcessingFailure(event.getDocumentId(), e.getMessage());
        }
    }
}
