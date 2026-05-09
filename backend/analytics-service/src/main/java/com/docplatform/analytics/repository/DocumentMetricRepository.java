package com.docplatform.analytics.repository;

import com.docplatform.analytics.model.DocumentMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
public interface DocumentMetricRepository extends JpaRepository<DocumentMetric, String> {

    long countByStatus(DocumentMetric.ProcessingStatus status);

    @Query("SELECT d.documentType, COUNT(d) as count FROM DocumentMetric d GROUP BY d.documentType")
    List<Object[]> countByDocumentType();

    @Query("SELECT AVG(d.processingTimeMs) FROM DocumentMetric d WHERE d.status = 'COMPLETED'")
    Double avgProcessingTimeMs();

    @Query("SELECT AVG(d.confidenceScore) FROM DocumentMetric d WHERE d.confidenceScore IS NOT NULL")
    Double avgConfidenceScore();

    @Query("SELECT d FROM DocumentMetric d WHERE d.uploadedAt >= :since ORDER BY d.uploadedAt DESC")
    List<DocumentMetric> findRecentDocuments(@Param("since") Instant since);

    @Query("SELECT COUNT(d) FROM DocumentMetric d WHERE d.hasAnomalies = true")
    long countWithAnomalies();

    @Query("SELECT d.llmModel, COUNT(d) as count, AVG(d.confidenceScore) as avgConf FROM DocumentMetric d GROUP BY d.llmModel")
    List<Object[]> llmModelStats();
}
