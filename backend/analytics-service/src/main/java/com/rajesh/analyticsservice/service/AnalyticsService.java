package com.rajesh.analyticsservice.service;

import com.rajesh.analyticsservice.dto.DashboardStatsDto;
import com.rajesh.analyticsservice.entity.DocumentMetric;
import com.rajesh.analyticsservice.repository.DocumentMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DocumentMetricRepository repository;

    public DashboardStatsDto getDashboardStats() {
        log.info("Computing dashboard statistics");

        long totalDocuments = repository.count();
        Instant startOfToday = LocalDate.now().atStartOfDay().toInstant(ZoneOffset.UTC);
        long documentsToday = repository.countDocumentsSince(startOfToday);

        Double avgProcessingTime = repository.avgProcessingTimeMs();
        Double avgConfidence     = repository.avgConfidenceScore();
        long anomalyCount        = repository.countDocumentsWithAnomalies();

        // Documents by type
        Map<String, Long> byType = new LinkedHashMap<>();
        repository.countByDocumentType().forEach(row ->
                byType.put((String) row[0], (Long) row[1]));

        // Documents by status
        Map<String, Long> byStatus = new LinkedHashMap<>();
        repository.countByStatus().forEach(row ->
                byStatus.put((String) row[0], (Long) row[1]));

        // 7-day trend
        List<DashboardStatsDto.TrendPoint> trend = buildDailyTrend(7);

        // LLM model stats
        List<DashboardStatsDto.LlmModelStat> llmStats = new ArrayList<>();
        repository.getLlmModelStats().forEach(row -> llmStats.add(
                DashboardStatsDto.LlmModelStat.builder()
                        .provider((String) row[0])
                        .count((Long) row[1])
                        .avgConfidence(row[2] != null ? ((Double) row[2]) : 0.0)
                        .avgProcessingTimeMs(row[3] != null ? ((Double) row[3]) : 0.0)
                        .build()
        ));

        long failedCount = byStatus.getOrDefault("FAILED", 0L);
        double errorRate = totalDocuments > 0
                ? (double) failedCount / totalDocuments * 100 : 0.0;

        return DashboardStatsDto.builder()
                .totalDocuments(totalDocuments)
                .documentsToday(documentsToday)
                .avgProcessingTimeMs(avgProcessingTime != null ? avgProcessingTime : 0.0)
                .avgConfidenceScore(avgConfidence != null ? avgConfidence : 0.0)
                .documentsWithAnomalies(anomalyCount)
                .errorRate(Math.round(errorRate * 10.0) / 10.0)
                .documentsByType(byType)
                .documentsByStatus(byStatus)
                .dailyTrend(trend)
                .llmModelStats(llmStats)
                .build();
    }

    private List<DashboardStatsDto.TrendPoint> buildDailyTrend(int days) {
        List<DashboardStatsDto.TrendPoint> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            Instant start = date.atStartOfDay().toInstant(ZoneOffset.UTC);
            Instant end   = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC);
            List<DocumentMetric> docs = repository.findByUploadedAtBetween(start, end);
            double avgConf = docs.stream()
                    .filter(d -> d.getConfidenceScore() != null)
                    .mapToDouble(DocumentMetric::getConfidenceScore)
                    .average().orElse(0.0);
            trend.add(DashboardStatsDto.TrendPoint.builder()
                    .date(date.toString())
                    .count(docs.size())
                    .avgConfidence(Math.round(avgConf * 1000.0) / 1000.0)
                    .build());
        }
        return trend;
    }

    public void saveMetric(DocumentMetric metric) {
        repository.findByDocumentId(metric.getDocumentId()).ifPresentOrElse(
                existing -> {
                    existing.setProcessingStatus(metric.getProcessingStatus());
                    existing.setLlmProvider(metric.getLlmProvider());
                    existing.setLlmModel(metric.getLlmModel());
                    existing.setConfidenceScore(metric.getConfidenceScore());
                    existing.setTokensUsed(metric.getTokensUsed());
                    existing.setProcessingTimeMs(metric.getProcessingTimeMs());
                    existing.setAnomalyCount(metric.getAnomalyCount());
                    existing.setEntityCount(metric.getEntityCount());
                    existing.setExtractedAt(metric.getExtractedAt());
                    repository.save(existing);
                },
                () -> repository.save(metric)
        );
    }
}
