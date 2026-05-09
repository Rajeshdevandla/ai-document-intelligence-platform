package com.docplatform.analytics.config;

import com.docplatform.analytics.model.DocumentMetric;
import com.docplatform.analytics.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerConfig {

    private final AnalyticsService analyticsService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "document.extracted", groupId = "analytics-service-group")
    public void onDocumentExtracted(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            DocumentMetric metric = DocumentMetric.builder()
                .documentId(node.path("documentId").asText())
                .documentType(node.path("document_type").asText())
                .confidenceScore(node.path("confidence_score").asDouble(0.0))
                .processingTimeMs(node.path("processing_time_ms").asLong(0))
                .llmModel(node.path("llm_model").asText())
                .hasAnomalies(!node.path("anomalies").isEmpty())
                .anomalyCount(node.path("anomalies").size())
                .status(DocumentMetric.ProcessingStatus.COMPLETED)
                .processedAt(Instant.now())
                .build();

            analyticsService.recordMetric(metric);
            log.info("Recorded analytics for documentId={}", metric.getDocumentId());
        } catch (Exception e) {
            log.error("Failed to record analytics metric", e);
        }
    }

    @KafkaListener(topics = "document.failed", groupId = "analytics-service-group")
    public void onDocumentFailed(String payload) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            DocumentMetric metric = DocumentMetric.builder()
                .documentId(node.path("documentId").asText())
                .status(DocumentMetric.ProcessingStatus.FAILED)
                .errorMessage(node.path("error").asText())
                .processedAt(Instant.now())
                .build();
            analyticsService.recordMetric(metric);
        } catch (Exception e) {
            log.error("Failed to record failure metric", e);
        }
    }
}
