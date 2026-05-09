package com.rajesh.uploadservice;

import com.rajesh.uploadservice.dto.UploadRequest;
import com.rajesh.uploadservice.dto.UploadResponse;
import com.rajesh.uploadservice.event.DocumentUploadedEvent;
import com.rajesh.uploadservice.service.DocumentEventPublisher;
import com.rajesh.uploadservice.service.DocumentService;
import com.rajesh.uploadservice.service.DynamoDbMetadataService;
import com.rajesh.uploadservice.service.S3UploadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock private S3UploadService s3UploadService;
    @Mock private DynamoDbMetadataService dynamoDbMetadataService;
    @Mock private DocumentEventPublisher documentEventPublisher;

    @InjectMocks
    private DocumentService documentService;

    private UploadRequest uploadRequest;

    @BeforeEach
    void setUp() {
        uploadRequest = new UploadRequest();
        uploadRequest.setFileName("test-invoice.pdf");
        uploadRequest.setContentType("application/pdf");
        uploadRequest.setFileSize(1024L);
        uploadRequest.setUploadedBy("rajesh@example.com");
    }

    @Test
    void initiateUpload_shouldReturnValidResponse() {
        when(s3UploadService.buildS3Key(anyString(), eq("test-invoice.pdf")))
                .thenReturn("uploads/some-uuid/test-invoice.pdf");
        when(s3UploadService.getBucketName())
                .thenReturn("ai-doc-intelligence-uploads");
        when(s3UploadService.generatePresignedUploadUrl(anyString(), anyString()))
                .thenReturn("https://s3.amazonaws.com/ai-doc-intelligence-uploads/uploads/some-uuid/test-invoice.pdf?X-Amz-Signature=abc123");
        when(documentEventPublisher.publishDocumentUploaded(any(DocumentUploadedEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        UploadResponse response = documentService.initiateUpload(uploadRequest);

        assertThat(response).isNotNull();
        assertThat(response.getDocumentId()).isNotBlank();
        assertThat(response.getPresignedUrl()).contains("https://s3.amazonaws.com");
        assertThat(response.getS3Key()).contains("uploads/");
        assertThat(response.getExpiresInSeconds()).isEqualTo(900);
        assertThat(response.getStatusCheckUrl()).contains("/api/v1/documents/");

        verify(dynamoDbMetadataService, times(1)).saveDocument(any());
        verify(documentEventPublisher, times(1)).publishDocumentUploaded(any(DocumentUploadedEvent.class));
    }

    @Test
    void initiateUpload_shouldPublishKafkaEventWithCorrectFields() {
        when(s3UploadService.buildS3Key(anyString(), anyString()))
                .thenReturn("uploads/abc/test-invoice.pdf");
        when(s3UploadService.getBucketName()).thenReturn("test-bucket");
        when(s3UploadService.generatePresignedUploadUrl(anyString(), anyString()))
                .thenReturn("https://presigned-url");
        when(documentEventPublisher.publishDocumentUploaded(any())).thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        documentService.initiateUpload(uploadRequest);

        verify(documentEventPublisher).publishDocumentUploaded(argThat(event ->
                event.getFileName().equals("test-invoice.pdf") &&
                event.getContentType().equals("application/pdf") &&
                event.getDocumentId() != null &&
                event.getTimestamp() != null
        ));
    }
}
