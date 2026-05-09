package com.docplatform.upload.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class Document {
    private String documentId;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String s3Key;
    private String s3Bucket;
    private DocumentStatus status;
    private String uploadedBy;
    private Instant uploadedAt;
    private Instant updatedAt;
    private String processingJobId;

    public enum DocumentStatus {
        PENDING, PROCESSING, OCR_COMPLETE, EXTRACTED, FAILED
    }
}
