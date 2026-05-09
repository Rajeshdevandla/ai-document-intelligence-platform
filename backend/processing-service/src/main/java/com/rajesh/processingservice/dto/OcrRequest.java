package com.rajesh.processingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrRequest {
    private String documentId;
    private String s3Key;
    private String s3Bucket;
    private String contentType;
    private String ocrEngine; // "textract" or "tesseract"
}
