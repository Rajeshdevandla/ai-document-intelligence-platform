package com.rajesh.analyticsservice.repository;

import com.rajesh.analyticsservice.entity.DocumentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentMetricRepository extends JpaRepository<DocumentMetric, Long> {

    Optional<DocumentMetric> findByDocumentId(String documentId);

    @Query("SELECT COUNT(d) FROM DocumentMetric d WHERE d.uploadedAt >= :since")
    long countDocumentsSince(@Param("since") Instant since);

    @Query("SELECT d.documentType, COUNT(d) FROM DocumentMetric d GROUP BY d.documentType")
    List<Object[]> countByDocumentType();

    @Query("SELECT AVG(d.processingTimeMs) FROM DocumentMetric d WHERE d.processingTimeMs IS NOT NULL")
    Double avgProcessingTimeMs();

    @Query("SELECT AVG(d.confidenceScore) FROM DocumentMetric d WHERE d.confidenceScore IS NOT NULL")
    Double avgConfidenceScore();

    @Query("SELECT d FROM DocumentMetric d ORDER BY d.createdAt DESC")
    List<DocumentMetric> findRecentDocuments(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(d) FROM DocumentMetric d WHERE d.anomalyCount > 0")
    long countDocumentsWithAnomalies();

    @Query("SELECT d.llmProvider, COUNT(d), AVG(d.confidenceScore), AVG(d.processingTimeMs) " +
           "FROM DocumentMetric d GROUP BY d.llmProvider")
    List<Object[]> getLlmModelStats();

    @Query("SELECT d.processingStatus, COUNT(d) FROM DocumentMetric d GROUP BY d.processingStatus")
    List<Object[]> countByStatus();

    List<DocumentMetric> findByUploadedAtBetween(Instant start, Instant end);
}
