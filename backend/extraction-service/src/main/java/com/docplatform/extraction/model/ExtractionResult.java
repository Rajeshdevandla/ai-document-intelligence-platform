package com.docplatform.extraction.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ExtractionResult {
    private String documentId;

    @JsonProperty("document_type")
    private String documentType;

    private List<Entity> entities;
    private List<DateField> dates;
    private Map<String, Object> totals;

    @JsonProperty("line_items")
    private List<Map<String, Object>> lineItems;

    private String summary;
    private List<String> anomalies;

    @JsonProperty("llm_model")
    private String llmModel;

    @JsonProperty("confidence_score")
    private Double confidenceScore;

    @JsonProperty("processing_time_ms")
    private Long processingTimeMs;

    private Instant extractedAt;

    @Data
    @Builder
    public static class Entity {
        private String type;
        private String value;
        private Double confidence;
    }

    @Data
    @Builder
    public static class DateField {
        private String label;
        private String value;
    }
}
