package com.docplatform.extraction.service;

import com.docplatform.extraction.dto.ExtractionRequest;
import com.docplatform.extraction.llm.LlmProvider;
import com.docplatform.extraction.model.ExtractionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExtractionService {

    private final List<LlmProvider> llmProviders;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${llm.provider:openai}")
    private String defaultProvider;

    private Map<String, LlmProvider> providerMap() {
        return llmProviders.stream()
                .collect(Collectors.toMap(LlmProvider::getProviderName, Function.identity()));
    }

    public ExtractionResult extract(ExtractionRequest request) {
        String providerName = request.getLlmProvider() != null
                ? request.getLlmProvider() : defaultProvider;

        LlmProvider provider = providerMap().getOrDefault(providerName, providerMap().get("openai"));
        log.info("Using LLM provider={} for documentId={}", providerName, request.getDocumentId());

        ExtractionResult result = provider.extract(request.getDocumentId(), request.getCleanedText());

        // Publish extracted event to Kafka
        kafkaTemplate.send("document.extracted", request.getDocumentId(), result);
        log.info("Published document.extracted for documentId={} type={} confidence={}",
                request.getDocumentId(), result.getDocumentType(), result.getConfidenceScore());

        return result;
    }
}
