package com.docplatform.processing.dto;

import lombok.Data;

@Data
public class DocumentUploadedEvent {
    private String documentId;
    private String s3Key;
    private String s3Bucket;
    private String fileName;
    private String fileType;
    private String uploadedBy;
    private String uploadedAt;
}
