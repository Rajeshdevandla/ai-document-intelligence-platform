package com.docplatform.upload.service;

import com.docplatform.upload.dto.UploadRequest;
import com.docplatform.upload.dto.UploadResponse;
import com.docplatform.upload.kafka.DocumentEventPublisher;
import com.docplatform.upload.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final S3UploadService s3UploadService;
    private final DocumentEventPublisher eventPublisher;
    private final DynamoDbMetadataService dynamoDbMetadataService;

    public UploadResponse initiateUpload(UploadRequest request, String userId) {
        String documentId = UUID.randomUUID().toString();

        // Step 1: Generate presigned URL
        S3UploadService.PresignedUploadResult s3Result =
                s3UploadService.generatePresignedUrl(request.getFileName(), request.getContentType());

        // Step 2: Persist metadata
        Document document = Document.builder()
                .documentId(documentId)
                .fileName(request.getFileName())
                .fileType(request.getContentType())
                .fileSize(request.getFileSize())
                .s3Key(s3Result.getS3Key())
                .s3Bucket(System.getProperty("aws.s3.bucket", "doc-intelligence-bucket"))
                .status(Document.DocumentStatus.PENDING)
                .uploadedBy(userId)
                .uploadedAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        dynamoDbMetadataService.saveDocument(document);

        // Step 3: Publish Kafka event
        eventPublisher.publishDocumentUploaded(document);

        log.info("Upload initiated: documentId={} userId={} s3Key={}",
                documentId, userId, s3Result.getS3Key());

        return UploadResponse.builder()
                .documentId(documentId)
                .presignedUrl(s3Result.getPresignedUrl())
                .s3Key(s3Result.getS3Key())
                .expiresInSeconds(s3Result.getExpiresInSeconds())
                .statusCheckUrl("/api/v1/documents/" + documentId + "/status")
                .build();
    }

    public Document getDocumentStatus(String documentId) {
        return dynamoDbMetadataService.getDocument(documentId);
    }
}
