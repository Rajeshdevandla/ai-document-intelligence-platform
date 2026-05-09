package com.rajesh.extractionservice.controller;

import com.rajesh.extractionservice.dto.ExtractionRequest;
import com.rajesh.extractionservice.model.ExtractionResult;
import com.rajesh.extractionservice.service.ExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/extract")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping
    public ResponseEntity<ExtractionResult> extract(@RequestBody ExtractionRequest request) {
        log.info("Extraction request for documentId: {}, provider: {}",
                request.getDocumentId(), request.getLlmProvider());
        ExtractionResult result = extractionService.extract(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "extraction-service",
                "version", "1.0.0"
        ));
    }
}
