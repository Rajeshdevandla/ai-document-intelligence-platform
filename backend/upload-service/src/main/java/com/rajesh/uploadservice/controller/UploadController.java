package com.rajesh.uploadservice.controller;

import com.rajesh.uploadservice.dto.UploadRequest;
import com.rajesh.uploadservice.dto.UploadResponse;
import com.rajesh.uploadservice.model.Document;
import com.rajesh.uploadservice.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UploadController {

    private final DocumentService documentService;

    @PostMapping("/upload-url")
    public ResponseEntity<UploadResponse> generateUploadUrl(@Valid @RequestBody UploadRequest request) {
        log.info("Received upload request for file: {}", request.getFileName());
        UploadResponse response = documentService.initiateUpload(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{documentId}/status")
    public ResponseEntity<?> getDocumentStatus(@PathVariable String documentId) {
        return documentService.getDocumentStatus(documentId)
                .map(doc -> ResponseEntity.ok(Map.of(
                        "documentId", doc.getDocumentId(),
                        "status",     doc.getStatus().name(),
                        "fileName",   doc.getFileName(),
                        "updatedAt",  doc.getUpdatedAt().toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status",  "UP",
                "service", "upload-service",
                "version", "1.0.0"
        ));
    }
}
