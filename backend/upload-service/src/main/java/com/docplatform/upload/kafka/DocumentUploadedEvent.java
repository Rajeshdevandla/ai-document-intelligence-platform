package com.docplatform.upload.kafka;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DocumentUploadedEvent {
    private String documentId;
    private String s3Key;
    private String s3Bucket;
    private String fileName;
    private String fileType;
    private String uploadedBy;
    private String uploadedAt;
}
