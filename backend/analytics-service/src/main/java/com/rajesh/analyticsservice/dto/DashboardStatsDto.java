package com.rajesh.analyticsservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private long totalDocuments;
    private long documentsToday;
    private double avgProcessingTimeMs;
    private double avgConfidenceScore;
    private long documentsWithAnomalies;
    private double errorRate;
    private Map<String, Long> documentsByType;
    private Map<String, Long> documentsByStatus;
    private List<TrendPoint> dailyTrend;
    private List<LlmModelStat> llmModelStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String date;
        private long count;
        private double avgConfidence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LlmModelStat {
        private String provider;
        private long count;
        private double avgConfidence;
        private double avgProcessingTimeMs;
    }
}
