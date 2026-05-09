package com.rajesh.extractionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionRequest {
    private String documentId;
    private String cleanedText;
    private String fileType;
    private String llmProvider; // "openai", "bedrock", "gemini"
}
