package com.docplatform.analytics.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "document_metrics")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentMetric {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String documentId;

    private String documentType;
    private String uploadedBy;
    private Double confidenceScore;
    private Long processingTimeMs;
    private Integer pageCount;
    private String llmModel;
    private Boolean hasAnomalies;
    private Integer anomalyCount;

    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;

    private String errorMessage;
    private Instant uploadedAt;
    private Instant processedAt;
    private Instant createdAt;

    public enum ProcessingStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
