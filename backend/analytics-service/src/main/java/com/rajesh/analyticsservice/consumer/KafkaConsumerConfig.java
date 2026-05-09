package com.rajesh.analyticsservice.consumer;

import com.rajesh.analyticsservice.entity.DocumentMetric;
import com.rajesh.analyticsservice.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaConsumerConfig {

    private final AnalyticsService analyticsService;

    @KafkaListener(topics = "document.extracted", groupId = "analytics-service-group")
    public void onDocumentExtracted(@Payload Map<String, Object> payload) {
        log.info("Received document.extracted event: {}", payload.get("documentId"));
        try {
            DocumentMetric metric = DocumentMetric.builder()
                    .documentId(String.valueOf(payload.get("documentId")))
                    .documentType(String.valueOf(payload.getOrDefault("documentType", "unknown")))
                    .processingStatus(DocumentMetric.ProcessingStatus.EXTRACTED)
                    .llmProvider(String.valueOf(payload.getOrDefault("llmProvider", "unknown")))
                    .llmModel(String.valueOf(payload.getOrDefault("llmModel", "unknown")))
                    .confidenceScore(parseDouble(payload.get("confidence")))
                    .tokensUsed(parseInteger(payload.get("tokensUsed")))
                    .processingTimeMs(parseLong(payload.get("processingTimeMs")))
                    .anomalyCount(parseListSize(payload.get("anomalies")))
                    .entityCount(parseListSize(payload.get("entities")))
                    .extractedAt(Instant.now())
                    .uploadedAt(Instant.now())
                    .build();
            analyticsService.saveMetric(metric);
        } catch (Exception e) {
            log.error("Error processing document.extracted event", e);
        }
    }

    @KafkaListener(topics = "document.failed", groupId = "analytics-service-group")
    public void onDocumentFailed(@Payload Map<String, Object> payload) {
        log.warn("Received document.failed event: {}", payload.get("documentId"));
        DocumentMetric metric = DocumentMetric.builder()
                .documentId(String.valueOf(payload.get("documentId")))
                .processingStatus(DocumentMetric.ProcessingStatus.FAILED)
                .uploadedAt(Instant.now())
                .build();
        analyticsService.saveMetric(metric);
    }

    private Double parseDouble(Object val) {
        if (val == null) return null;
        try { return Double.parseDouble(val.toString()); } catch (Exception e) { return null; }
    }

    private Integer parseInteger(Object val) {
        if (val == null) return null;
        try { return Integer.parseInt(val.toString()); } catch (Exception e) { return null; }
    }

    private Long parseLong(Object val) {
        if (val == null) return null;
        try { return Long.parseLong(val.toString()); } catch (Exception e) { return null; }
    }

    @SuppressWarnings("unchecked")
    private Integer parseListSize(Object val) {
        if (val instanceof java.util.List) return ((java.util.List<?>) val).size();
        return 0;
    }
}
