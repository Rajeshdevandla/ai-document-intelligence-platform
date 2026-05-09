package com.docplatform.analytics.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class DashboardStatsDto {
    private long totalDocuments;
    private long completedDocuments;
    private long failedDocuments;
    private long pendingDocuments;
    private double avgProcessingTimeMs;
    private double avgConfidenceScore;
    private double errorRate;
    private long documentsWithAnomalies;
    private Map<String, Long> documentTypeBreakdown;
    private List<TrendPoint> dailyTrend;
    private List<LlmModelStat> llmModelStats;

    @Data
    @Builder
    public static class TrendPoint {
        private String date;
        private long count;
        private double avgConfidence;
    }

    @Data
    @Builder
    public static class LlmModelStat {
        private String model;
        private long documentCount;
        private double avgConfidence;
    }
}
