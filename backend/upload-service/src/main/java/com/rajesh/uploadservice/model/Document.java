package com.rajesh.uploadservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {
    private String documentId;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String s3Key;
    private String s3Bucket;
    private DocumentStatus status;
    private String uploadedBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String processingNotes;

    public enum DocumentStatus {
        PENDING, PROCESSING, OCR_COMPLETE, EXTRACTED, FAILED
    }
}
