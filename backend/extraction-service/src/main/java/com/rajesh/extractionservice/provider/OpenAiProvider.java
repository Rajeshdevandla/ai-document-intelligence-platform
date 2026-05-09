package com.rajesh.extractionservice.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rajesh.extractionservice.model.ExtractionResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component("openai")
@RequiredArgsConstructor
public class OpenAiProvider implements LlmProvider {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.model:gpt-4o}")
    private String model;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    private static final String EXTRACTION_PROMPT = """
            You are an expert document extraction model. Analyze the following document text and extract structured information.
            
            Return a JSON object with exactly these fields:
            {
              "document_type": "invoice|receipt|contract|report|letter|other",
              "entities": [{"type": "PERSON|ORG|LOCATION|DATE|MONEY", "value": "...", "confidence": 0.95}],
              "dates": [{"label": "invoice_date|due_date|...", "value": "...", "normalized": "YYYY-MM-DD"}],
              "totals": {"subtotal": 0.0, "tax": 0.0, "total": 0.0, "currency": "USD"},
              "line_items": [{"description": "...", "quantity": 1, "unit_price": 0.0, "total": 0.0}],
              "summary": "A concise 2-3 sentence summary of the document",
              "anomalies": ["List any inconsistencies or suspicious findings"]
            }
            
            Document text:
            """;

    @Override
    public ExtractionResult extract(String documentId, String cleanedText, String fileType) {
        long startTime = System.currentTimeMillis();
        log.info("Extracting with OpenAI GPT-4o for documentId: {}", documentId);

        try {
            Map<String, Object> message = Map.of(
                "role", "user",
                "content", EXTRACTION_PROMPT + cleanedText
            );

            Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(message),
                "response_format", Map.of("type", "json_object"),
                "temperature", 0.1,
                "max_tokens", 2000
            );

            String responseStr = webClientBuilder.build()
                    .post()
                    .uri(baseUrl + "/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            return parseOpenAiResponse(documentId, responseStr, System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            log.error("OpenAI extraction failed for documentId: {}", documentId, e);
            return buildErrorResult(documentId, e.getMessage(), System.currentTimeMillis() - startTime);
        }
    }

    private ExtractionResult parseOpenAiResponse(String documentId, String responseStr, long processingTimeMs) {
        try {
            JsonNode root = objectMapper.readTree(responseStr);
            int tokensUsed = root.path("usage").path("total_tokens").asInt(0);
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode extracted = objectMapper.readTree(content);

            List<ExtractionResult.Entity> entities = new ArrayList<>();
            extracted.path("entities").forEach(e -> entities.add(
                ExtractionResult.Entity.builder()
                    .type(e.path("type").asText())
                    .value(e.path("value").asText())
                    .confidence(e.path("confidence").asDouble(0.9))
                    .build()
            ));

            List<ExtractionResult.DateField> dates = new ArrayList<>();
            extracted.path("dates").forEach(d -> dates.add(
                ExtractionResult.DateField.builder()
                    .label(d.path("label").asText())
                    .value(d.path("value").asText())
                    .normalized(d.path("normalized").asText())
                    .build()
            ));

            List<String> anomalies = new ArrayList<>();
            extracted.path("anomalies").forEach(a -> anomalies.add(a.asText()));

            Map<String, Object> totals = objectMapper.convertValue(
                extracted.path("totals"), Map.class);

            return ExtractionResult.builder()
                    .documentId(documentId)
                    .documentType(extracted.path("document_type").asText("unknown"))
                    .entities(entities)
                    .dates(dates)
                    .totals(totals)
                    .summary(extracted.path("summary").asText())
                    .anomalies(anomalies)
                    .llmProvider("openai")
                    .llmModel(model)
                    .confidence(0.94)
                    .tokensUsed(tokensUsed)
                    .processingTimeMs(processingTimeMs)
                    .extractedAt(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse OpenAI response", e);
            return buildErrorResult(documentId, "Parse error: " + e.getMessage(), processingTimeMs);
        }
    }

    private ExtractionResult buildErrorResult(String documentId, String error, long processingTimeMs) {
        return ExtractionResult.builder()
                .documentId(documentId)
                .documentType("unknown")
                .entities(Collections.emptyList())
                .dates(Collections.emptyList())
                .totals(Collections.emptyMap())
                .anomalies(List.of("Extraction failed: " + error))
                .summary("Extraction failed")
                .llmProvider("openai")
                .llmModel(model)
                .confidence(0.0)
                .processingTimeMs(processingTimeMs)
                .extractedAt(Instant.now())
                .build();
    }

    @Override public String getProviderName() { return "openai"; }
    @Override public String getModelName()    { return model; }
}
