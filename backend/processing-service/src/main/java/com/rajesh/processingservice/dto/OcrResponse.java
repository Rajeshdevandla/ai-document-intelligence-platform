package com.rajesh.processingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrResponse {
    private String documentId;
    private String rawText;
    private String cleanedText;
    private String documentType;
    private double ocrConfidence;
    private long processingTimeMs;
    private String ocrEngine;
    private boolean success;
    private String errorMessage;
}
