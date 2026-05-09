package com.docplatform.analytics.service;

import com.docplatform.analytics.dto.DashboardStatsDto;
import com.docplatform.analytics.model.DocumentMetric;
import com.docplatform.analytics.repository.DocumentMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final DocumentMetricRepository repository;

    public DashboardStatsDto getDashboardStats() {
        long total       = repository.count();
        long completed   = repository.countByStatus(DocumentMetric.ProcessingStatus.COMPLETED);
        long failed      = repository.countByStatus(DocumentMetric.ProcessingStatus.FAILED);
        long pending     = repository.countByStatus(DocumentMetric.ProcessingStatus.PENDING);
        long withAnomalies = repository.countWithAnomalies();

        Double avgTime = repository.avgProcessingTimeMs();
        Double avgConf = repository.avgConfidenceScore();
        double errorRate = total > 0 ? (double) failed / total * 100 : 0;

        // Document type breakdown
        Map<String, Long> typeBreakdown = new LinkedHashMap<>();
        repository.countByDocumentType().forEach(row ->
            typeBreakdown.put((String) row[0], (Long) row[1])
        );

        // LLM model stats
        List<DashboardStatsDto.LlmModelStat> modelStats = repository.llmModelStats().stream()
            .map(row -> DashboardStatsDto.LlmModelStat.builder()
                .model((String) row[0])
                .documentCount((Long) row[1])
                .avgConfidence(row[2] != null ? (Double) row[2] : 0.0)
                .build())
            .collect(Collectors.toList());

        // Last 7-day trend (simplified)
        List<DashboardStatsDto.TrendPoint> trend = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            Instant since = Instant.now().minus(i + 1, ChronoUnit.DAYS);
            Instant until = Instant.now().minus(i, ChronoUnit.DAYS);
            List<DocumentMetric> docs = repository.findRecentDocuments(since);
            long dayCount = docs.stream()
                .filter(d -> d.getUploadedAt() != null && d.getUploadedAt().isBefore(until))
                .count();
            trend.add(DashboardStatsDto.TrendPoint.builder()
                .date(since.truncatedTo(ChronoUnit.DAYS).toString().substring(0, 10))
                .count(dayCount)
                .avgConfidence(avgConf != null ? avgConf : 0.0)
                .build());
        }

        return DashboardStatsDto.builder()
            .totalDocuments(total)
            .completedDocuments(completed)
            .failedDocuments(failed)
            .pendingDocuments(pending)
            .avgProcessingTimeMs(avgTime != null ? avgTime : 0)
            .avgConfidenceScore(avgConf != null ? avgConf : 0)
            .errorRate(errorRate)
            .documentsWithAnomalies(withAnomalies)
            .documentTypeBreakdown(typeBreakdown)
            .dailyTrend(trend)
            .llmModelStats(modelStats)
            .build();
    }

    public void recordMetric(DocumentMetric metric) {
        metric.setCreatedAt(Instant.now());
        repository.save(metric);
        log.debug("Recorded metric for documentId={}", metric.getDocumentId());
    }
}
