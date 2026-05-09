package com.rajesh.uploadservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {
    private String documentId;
    private String presignedUrl;
    private String s3Key;
    private int expiresInSeconds;
    private String statusCheckUrl;
    private String message;
}
