package com.docplatform.processing.dto;

import lombok.Data;

@Data
public class OcrResponse {
    private String documentId;
    private String rawText;
    private String cleanedText;
    private Double confidence;
    private Integer pageCount;
    private String ocrEngine;
    private Long processingTimeMs;
    private boolean success;
    private String errorMessage;
}
