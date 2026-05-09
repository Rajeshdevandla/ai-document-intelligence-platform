package com.docplatform.upload.kafka;

import com.docplatform.upload.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.document-uploaded:document.uploaded}")
    private String documentUploadedTopic;

    public void publishDocumentUploaded(Document document) {
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .documentId(document.getDocumentId())
                .s3Key(document.getS3Key())
                .s3Bucket(document.getS3Bucket())
                .fileName(document.getFileName())
                .fileType(document.getFileType())
                .uploadedBy(document.getUploadedBy())
                .uploadedAt(document.getUploadedAt().toString())
                .build();

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(documentUploadedTopic, document.getDocumentId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Published document.uploaded event for documentId={} partition={} offset={}",
                        document.getDocumentId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish event for documentId={}", document.getDocumentId(), ex);
            }
        });
    }
}
