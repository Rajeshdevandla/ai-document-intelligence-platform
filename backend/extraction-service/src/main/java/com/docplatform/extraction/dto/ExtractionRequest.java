package com.docplatform.extraction.dto;

import lombok.Data;

@Data
public class ExtractionRequest {
    private String documentId;
    private String cleanedText;
    private String fileType;
    private String llmProvider; // openai | bedrock | gemini — optional override
}
