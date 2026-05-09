package com.rajesh.processingservice.service;

import com.rajesh.processingservice.dto.DocumentUploadedEvent;
import com.rajesh.processingservice.dto.OcrRequest;
import com.rajesh.processingservice.dto.OcrResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrOrchestrationService {

    private final WebClient.Builder webClientBuilder;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${python.pipeline.url:http://python-pipeline:8090}")
    private String pythonPipelineUrl;

    public void processDocument(DocumentUploadedEvent event) {
        log.info("Starting OCR orchestration for documentId: {}", event.getDocumentId());

        OcrRequest ocrRequest = OcrRequest.builder()
                .documentId(event.getDocumentId())
                .s3Key(event.getS3Key())
                .s3Bucket(event.getS3Bucket())
                .contentType(event.getContentType())
                .ocrEngine("textract")
                .build();

        webClientBuilder.build()
                .post()
                .uri(pythonPipelineUrl + "/process")
                .bodyValue(ocrRequest)
                .retrieve()
                .bodyToMono(OcrResponse.class)
                .doOnNext(response -> {
                    log.info("OCR completed for documentId: {}, confidence: {}",
                            response.getDocumentId(), response.getOcrConfidence());
                    publishOcrCompleted(response);
                })
                .doOnError(error -> {
                    log.error("OCR failed for documentId: {}", event.getDocumentId(), error);
                    kafkaTemplate.send("document.failed", event.getDocumentId(), event);
                })
                .onErrorResume(ex -> Mono.empty())
                .subscribe();
    }

    private void publishOcrCompleted(OcrResponse response) {
        kafkaTemplate.send("document.ocr.completed", response.getDocumentId(), response);
        log.info("Published document.ocr.completed for documentId: {}", response.getDocumentId());
    }
}
