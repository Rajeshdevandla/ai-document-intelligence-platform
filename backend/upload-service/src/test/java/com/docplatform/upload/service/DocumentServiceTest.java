package com.docplatform.upload.service;

import com.docplatform.upload.dto.UploadRequest;
import com.docplatform.upload.dto.UploadResponse;
import com.docplatform.upload.kafka.DocumentEventPublisher;
import com.docplatform.upload.model.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentService Unit Tests")
class DocumentServiceTest {

    @Mock private S3UploadService s3UploadService;
    @Mock private DocumentEventPublisher eventPublisher;
    @Mock private DynamoDbMetadataService dynamoDbMetadataService;
    @InjectMocks private DocumentService documentService;

    private UploadRequest uploadRequest;

    @BeforeEach
    void setUp() {
        uploadRequest = new UploadRequest();
        uploadRequest.setFileName("invoice.pdf");
        uploadRequest.setContentType("application/pdf");
        uploadRequest.setFileSize(1024L);
    }

    @Test
    @DisplayName("Should generate presigned URL and publish Kafka event on upload initiation")
    void initiateUpload_shouldReturnPresignedUrlAndPublishEvent() {
        // Given
        S3UploadService.PresignedUploadResult s3Result = S3UploadService.PresignedUploadResult.builder()
                .presignedUrl("https://s3.amazonaws.com/presigned-url")
                .s3Key("uploads/uuid/invoice.pdf")
                .expiresInSeconds(900L)
                .build();
        when(s3UploadService.generatePresignedUrl(anyString(), anyString())).thenReturn(s3Result);
        doNothing().when(dynamoDbMetadataService).saveDocument(any());
        doNothing().when(eventPublisher).publishDocumentUploaded(any());

        // When
        UploadResponse response = documentService.initiateUpload(uploadRequest, "user123");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getDocumentId()).isNotNull().isNotEmpty();
        assertThat(response.getPresignedUrl()).contains("presigned-url");
        assertThat(response.getExpiresInSeconds()).isEqualTo(900L);
        assertThat(response.getStatusCheckUrl()).startsWith("/api/v1/documents/");

        verify(s3UploadService, times(1)).generatePresignedUrl("invoice.pdf", "application/pdf");
        verify(dynamoDbMetadataService, times(1)).saveDocument(any(Document.class));
        verify(eventPublisher, times(1)).publishDocumentUploaded(any(Document.class));
    }

    @Test
    @DisplayName("Should set document status to PENDING on initiation")
    void initiateUpload_shouldSetStatusToPending() {
        // Given
        S3UploadService.PresignedUploadResult s3Result = S3UploadService.PresignedUploadResult.builder()
                .presignedUrl("https://s3.amazonaws.com/presigned-url")
                .s3Key("uploads/uuid/invoice.pdf")
                .expiresInSeconds(900L)
                .build();
        when(s3UploadService.generatePresignedUrl(anyString(), anyString())).thenReturn(s3Result);

        // Capture the saved document to assert its status
        org.mockito.ArgumentCaptor<Document> documentCaptor =
                org.mockito.ArgumentCaptor.forClass(Document.class);
        doNothing().when(dynamoDbMetadataService).saveDocument(documentCaptor.capture());
        doNothing().when(eventPublisher).publishDocumentUploaded(any());

        // When
        documentService.initiateUpload(uploadRequest, "user123");

        // Then
        Document capturedDoc = documentCaptor.getValue();
        assertThat(capturedDoc.getStatus()).isEqualTo(Document.DocumentStatus.PENDING);
        assertThat(capturedDoc.getUploadedBy()).isEqualTo("user123");
        assertThat(capturedDoc.getFileName()).isEqualTo("invoice.pdf");
    }
}
