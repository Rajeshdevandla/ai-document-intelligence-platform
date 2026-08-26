package com.rajesh.processingservice.consumer;

import com.rajesh.processingservice.dto.DocumentUploadedEvent;
import com.rajesh.processingservice.service.OcrOrchestrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DocumentUploadedConsumerTest {

    @Mock
    private OcrOrchestrationService ocrOrchestrationService;

    @InjectMocks
    private DocumentUploadedConsumer consumer;

    @Test
    void delegatesUploadedDocumentToOcrOrchestration() {
        DocumentUploadedEvent event = uploadedDocument();

        consumer.consume(event, 2, 17L);

        verify(ocrOrchestrationService).processDocument(event);
    }

    @Test
    void containsOcrOrchestrationFailures() {
        DocumentUploadedEvent event = uploadedDocument();
        doThrow(new IllegalStateException("OCR unavailable"))
                .when(ocrOrchestrationService)
                .processDocument(event);

        assertDoesNotThrow(() -> consumer.consume(event, 2, 17L));
        verify(ocrOrchestrationService).processDocument(event);
    }

    private DocumentUploadedEvent uploadedDocument() {
        return DocumentUploadedEvent.builder()
                .documentId("doc-123")
                .s3Bucket("documents")
                .s3Key("uploads/doc-123.pdf")
                .fileName("doc-123.pdf")
                .contentType("application/pdf")
                .build();
    }
}
