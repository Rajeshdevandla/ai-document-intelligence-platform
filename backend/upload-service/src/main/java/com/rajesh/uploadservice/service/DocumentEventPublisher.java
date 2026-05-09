package com.rajesh.uploadservice.service;

import com.rajesh.uploadservice.event.DocumentUploadedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentEventPublisher {

    private static final String TOPIC_DOCUMENT_UPLOADED = "document.uploaded";
    private static final String TOPIC_DOCUMENT_FAILED   = "document.failed";

    private final KafkaTemplate<String, DocumentUploadedEvent> kafkaTemplate;

    public CompletableFuture<SendResult<String, DocumentUploadedEvent>> publishDocumentUploaded(
            DocumentUploadedEvent event) {

        log.info("Publishing document.uploaded event for documentId: {}", event.getDocumentId());

        CompletableFuture<SendResult<String, DocumentUploadedEvent>> future =
                kafkaTemplate.send(TOPIC_DOCUMENT_UPLOADED, event.getDocumentId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Event published successfully to topic: {}, partition: {}, offset: {}",
                        TOPIC_DOCUMENT_UPLOADED,
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event for documentId: {}", event.getDocumentId(), ex);
            }
        });

        return future;
    }

    public void publishDocumentFailed(String documentId, String reason) {
        log.warn("Publishing document.failed event for documentId: {}, reason: {}", documentId, reason);
        // Simplified failed event — uses same POJO with null fields as a failure marker
        DocumentUploadedEvent failedEvent = DocumentUploadedEvent.builder()
                .documentId(documentId)
                .build();
        kafkaTemplate.send(TOPIC_DOCUMENT_FAILED, documentId, failedEvent);
    }
}
