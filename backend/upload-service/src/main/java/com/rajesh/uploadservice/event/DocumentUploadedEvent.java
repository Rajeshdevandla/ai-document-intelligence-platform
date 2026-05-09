package com.rajesh.uploadservice.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadedEvent {
    private String documentId;
    private String s3Key;
    private String s3Bucket;
    private String fileName;
    private String contentType;
    private Long fileSize;
    private String uploadedBy;
    private Instant timestamp;
}
