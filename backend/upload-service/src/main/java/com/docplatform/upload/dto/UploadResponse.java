package com.docplatform.upload.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {
    private String documentId;
    private String presignedUrl;
    private String s3Key;
    private Long expiresInSeconds;
    private String statusCheckUrl;
}
