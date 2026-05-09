package com.rajesh.extractionservice.service;

import com.rajesh.extractionservice.dto.ExtractionRequest;
import com.rajesh.extractionservice.model.ExtractionResult;
import com.rajesh.extractionservice.provider.LlmProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractionService {

    private final Map<String, LlmProvider> providers;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${llm.default-provider:openai}")
    private String defaultProvider;

    public ExtractionResult extract(ExtractionRequest request) {
        String providerName = request.getLlmProvider() != null
                ? request.getLlmProvider().toLowerCase()
                : defaultProvider;

        LlmProvider provider = providers.get(providerName);
        if (provider == null) {
            log.warn("Unknown provider '{}', falling back to '{}'", providerName, defaultProvider);
            provider = providers.get(defaultProvider);
        }

        log.info("Extracting documentId: {} using provider: {}", request.getDocumentId(), providerName);
        ExtractionResult result = provider.extract(
                request.getDocumentId(),
                request.getCleanedText(),
                request.getFileType()
        );

        // Publish to document.extracted topic
        kafkaTemplate.send("document.extracted", result.getDocumentId(), result);
        log.info("Published document.extracted for documentId: {}", result.getDocumentId());

        return result;
    }
}
