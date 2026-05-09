package com.rajesh.extractionservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExtractionResult {
    private String documentId;
    private String documentType;
    private List<Entity> entities;
    private List<DateField> dates;
    private Map<String, Object> totals;
    private List<Map<String, Object>> lineItems;
    private String summary;
    private List<String> anomalies;
    private String llmProvider;
    private String llmModel;
    private double confidence;
    private int tokensUsed;
    private long processingTimeMs;
    private Instant extractedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Entity {
        private String type;   // PERSON, ORG, LOCATION, etc.
        private String value;
        private double confidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DateField {
        private String label;  // invoice_date, due_date, etc.
        private String value;
        private String normalized; // ISO 8601
    }
}
