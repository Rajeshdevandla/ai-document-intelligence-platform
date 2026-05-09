package com.docplatform.extraction.consumer;

import com.docplatform.extraction.dto.ExtractionRequest;
import com.docplatform.extraction.model.ExtractionResult;
import com.docplatform.extraction.service.ExtractionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ExtractionController {

    private final ExtractionService extractionService;

    @PostMapping("/extract")
    public ResponseEntity<ExtractionResult> extract(@RequestBody ExtractionRequest request) {
        log.info("Extraction request received for documentId={}", request.getDocumentId());
        ExtractionResult result = extractionService.extract(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("extraction-service is UP");
    }
}
