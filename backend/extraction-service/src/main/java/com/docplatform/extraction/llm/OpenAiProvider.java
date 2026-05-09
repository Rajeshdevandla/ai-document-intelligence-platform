package com.docplatform.extraction.llm;

import com.docplatform.extraction.model.ExtractionResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiProvider implements LlmProvider {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.model:gpt-4o}")
    private String model;

    private static final String EXTRACTION_PROMPT = """
            You are an expert document extraction model. Analyze the following document text and extract structured data.
            
            Return ONLY a valid JSON object with these exact fields:
            {
              "document_type": "<Invoice|Contract|Receipt|Report|Letter|Other>",
              "entities": [{"type": "<ORGANIZATION|PERSON|LOCATION>", "value": "<value>", "confidence": 0.0}],
              "dates": [{"label": "<invoice_date|due_date|contract_date|etc>", "value": "<YYYY-MM-DD>"}],
              "totals": {"subtotal": 0.0, "tax": 0.0, "total": 0.0, "currency": "USD"},
              "line_items": [{"description": "", "quantity": 0, "unit_price": 0.0, "total": 0.0}],
              "summary": "<one sentence summary>",
              "anomalies": ["<any suspicious or inconsistent findings>"]
            }
            
            Document text:
            """;

    @Override
    public ExtractionResult extract(String documentId, String text) {
        long startTime = System.currentTimeMillis();
        log.info("Calling OpenAI {} for documentId={}", model, documentId);

        try {
            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                    Map.of("role", "user", "content", EXTRACTION_PROMPT + text)
                ),
                "temperature", 0.1,
                "response_format", Map.of("type", "json_object")
            );

            String responseJson = webClientBuilder.build()
                .post()
                .uri("https://api.openai.com/v1/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

            JsonNode root = objectMapper.readTree(responseJson);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode extracted = objectMapper.readTree(content);

            return buildResult(documentId, extracted, System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            log.error("OpenAI extraction failed for documentId={}", documentId, e);
            throw new RuntimeException("LLM extraction failed: " + e.getMessage(), e);
        }
    }

    private ExtractionResult buildResult(String documentId, JsonNode node, long durationMs) {
        List<ExtractionResult.Entity> entities = new ArrayList<>();
        if (node.has("entities")) {
            node.get("entities").forEach(e -> entities.add(
                ExtractionResult.Entity.builder()
                    .type(e.path("type").asText())
                    .value(e.path("value").asText())
                    .confidence(e.path("confidence").asDouble(0.9))
                    .build()
            ));
        }

        List<ExtractionResult.DateField> dates = new ArrayList<>();
        if (node.has("dates")) {
            node.get("dates").forEach(d -> dates.add(
                ExtractionResult.DateField.builder()
                    .label(d.path("label").asText())
                    .value(d.path("value").asText())
                    .build()
            ));
        }

        List<String> anomalies = new ArrayList<>();
        if (node.has("anomalies")) {
            node.get("anomalies").forEach(a -> anomalies.add(a.asText()));
        }

        return ExtractionResult.builder()
            .documentId(documentId)
            .documentType(node.path("document_type").asText("Unknown"))
            .entities(entities)
            .dates(dates)
            .totals(objectMapper.convertValue(node.path("totals"), Map.class))
            .summary(node.path("summary").asText())
            .anomalies(anomalies)
            .llmModel(model)
            .confidenceScore(0.92)
            .processingTimeMs(durationMs)
            .extractedAt(Instant.now())
            .build();
    }

    @Override
    public String getProviderName() {
        return "openai";
    }
}
