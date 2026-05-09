package com.rajesh.uploadservice.service;

import com.rajesh.uploadservice.dto.UploadRequest;
import com.rajesh.uploadservice.dto.UploadResponse;
import com.rajesh.uploadservice.event.DocumentUploadedEvent;
import com.rajesh.uploadservice.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final S3UploadService s3UploadService;
    private final DynamoDbMetadataService dynamoDbMetadataService;
    private final DocumentEventPublisher documentEventPublisher;

    @Value("${app.base-url:http://localhost:8081}")
    private String baseUrl;

    public UploadResponse initiateUpload(UploadRequest request) {
        String documentId = UUID.randomUUID().toString();
        String s3Key = s3UploadService.buildS3Key(documentId, request.getFileName());
        String bucketName = s3UploadService.getBucketName();

        log.info("Initiating upload for documentId: {}, file: {}", documentId, request.getFileName());

        // Generate presigned URL
        String presignedUrl = s3UploadService.generatePresignedUploadUrl(s3Key, request.getContentType());

        // Build document record
        Instant now = Instant.now();
        Document document = Document.builder()
                .documentId(documentId)
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .s3Key(s3Key)
                .s3Bucket(bucketName)
                .status(Document.DocumentStatus.PENDING)
                .uploadedBy(request.getUploadedBy())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Save metadata to DynamoDB
        dynamoDbMetadataService.saveDocument(document);

        // Publish Kafka event
        DocumentUploadedEvent event = DocumentUploadedEvent.builder()
                .documentId(documentId)
                .s3Key(s3Key)
                .s3Bucket(bucketName)
                .fileName(request.getFileName())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .uploadedBy(request.getUploadedBy())
                .timestamp(now)
                .build();

        documentEventPublisher.publishDocumentUploaded(event);

        return UploadResponse.builder()
                .documentId(documentId)
                .presignedUrl(presignedUrl)
                .s3Key(s3Key)
                .expiresInSeconds(900)
                .statusCheckUrl(baseUrl + "/api/v1/documents/" + documentId + "/status")
                .message("Upload URL generated. Use the presignedUrl to upload your file directly to S3.")
                .build();
    }

    public Optional<Document> getDocumentStatus(String documentId) {
        return dynamoDbMetadataService.findById(documentId);
    }
}
