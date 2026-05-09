package com.docplatform.upload.controller;

import com.docplatform.upload.dto.UploadRequest;
import com.docplatform.upload.dto.UploadResponse;
import com.docplatform.upload.model.Document;
import com.docplatform.upload.service.DocumentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UploadController {

    private final DocumentService documentService;

    /**
     * POST /api/v1/documents/upload-url
     * Returns a presigned S3 URL; client uploads the file directly to S3.
     */
    @PostMapping("/upload-url")
    public ResponseEntity<UploadResponse> getUploadUrl(
            @Valid @RequestBody UploadRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String userId = userDetails != null ? userDetails.getUsername() : "anonymous";
        UploadResponse response = documentService.initiateUpload(request, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/v1/documents/{documentId}/status
     * Polls processing status of a specific document.
     */
    @GetMapping("/{documentId}/status")
    public ResponseEntity<Document> getStatus(@PathVariable String documentId) {
        Document document = documentService.getDocumentStatus(documentId);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("upload-service is UP");
    }
}
