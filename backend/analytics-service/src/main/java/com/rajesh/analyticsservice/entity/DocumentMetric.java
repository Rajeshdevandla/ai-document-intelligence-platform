package com.rajesh.analyticsservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "document_metrics")
public class DocumentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", unique = true, nullable = false)
    private String documentId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "document_type")
    private String documentType;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Enumerated(EnumType.STRING)
    @Column(name = "processing_status")
    private ProcessingStatus processingStatus;

    @Column(name = "llm_provider")
    private String llmProvider;

    @Column(name = "llm_model")
    private String llmModel;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "tokens_used")
    private Integer tokensUsed;

    @Column(name = "processing_time_ms")
    private Long processingTimeMs;

    @Column(name = "anomaly_count")
    private Integer anomalyCount;

    @Column(name = "entity_count")
    private Integer entityCount;

    @Column(name = "uploaded_at")
    private Instant uploadedAt;

    @Column(name = "extracted_at")
    private Instant extractedAt;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
    }

    public enum ProcessingStatus {
        PENDING, PROCESSING, OCR_COMPLETE, EXTRACTED, FAILED
    }
}
