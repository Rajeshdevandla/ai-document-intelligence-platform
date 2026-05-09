package com.docplatform.processing.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OcrRequest {
    private String documentId;
    private String s3Key;
    private String s3Bucket;
    private String fileType;
}
