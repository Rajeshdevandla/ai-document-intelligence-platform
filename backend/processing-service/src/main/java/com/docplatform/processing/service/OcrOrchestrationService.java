package com.docplatform.processing.service;

import com.docplatform.processing.dto.DocumentUploadedEvent;
import com.docplatform.processing.dto.OcrRequest;
import com.docplatform.processing.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrOrchestrationService {

    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${python.pipeline.url:http://localhost:8090}")
    private String pythonPipelineUrl;

    @Value("${extraction.service.url:http://localhost:8083}")
    private String extractionServiceUrl;

    public void processDocument(DocumentUploadedEvent event) {
        log.info("Starting OCR orchestration for documentId={}", event.getDocumentId());

        // Step 1: Call Python OCR pipeline
        OcrRequest ocrRequest = OcrRequest.builder()
                .documentId(event.getDocumentId())
                .s3Key(event.getS3Key())
                .s3Bucket(event.getS3Bucket())
                .fileType(event.getFileType())
                .build();

        OcrResponse ocrResponse = webClientBuilder.build()
                .post()
                .uri(pythonPipelineUrl + "/process")
                .bodyValue(ocrRequest)
                .retrieve()
                .bodyToMono(OcrResponse.class)
                .block();

        if (ocrResponse == null || !ocrResponse.isSuccess()) {
            String errorMsg = ocrResponse != null ? ocrResponse.getErrorMessage() : "Null response from OCR";
            throw new RuntimeException("OCR failed: " + errorMsg);
        }

        log.info("OCR completed for documentId={} confidence={} pages={}",
                event.getDocumentId(), ocrResponse.getConfidence(), ocrResponse.getPageCount());

        // Step 2: Publish ocr.completed event
        kafkaTemplate.send("document.ocr.completed", event.getDocumentId(), ocrResponse);

        // Step 3: Forward cleaned text to extraction service
        webClientBuilder.build()
                .post()
                .uri(extractionServiceUrl + "/api/v1/extract")
                .bodyValue(Map.of(
                    "documentId", event.getDocumentId(),
                    "cleanedText", ocrResponse.getCleanedText(),
                    "fileType", event.getFileType()
                ))
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                    v -> log.info("Extraction triggered for documentId={}", event.getDocumentId()),
                    e -> log.error("Failed to trigger extraction for documentId={}", event.getDocumentId(), e)
                );
    }

    public void handleProcessingFailure(String documentId, String errorMessage) {
        kafkaTemplate.send("document.failed", documentId,
                Map.of("documentId", documentId, "error", errorMessage, "stage", "processing"));
    }
}
