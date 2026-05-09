# API Contracts — AI Document Intelligence Platform

## Upload Service (port 8081)

### POST /api/v1/documents/upload-url
Generate a presigned S3 URL for direct client upload.

**Request:**
```json
{
  "fileName": "invoice-2024.pdf",
  "contentType": "application/pdf",
  "fileSize": 204800,
  "uploadedBy": "user@example.com"
}
```

**Response:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "presignedUrl": "https://s3.amazonaws.com/bucket/uploads/...?X-Amz-Signature=...",
  "s3Key": "uploads/550e8400/invoice-2024.pdf",
  "expiresInSeconds": 900,
  "statusCheckUrl": "http://localhost:8081/api/v1/documents/550e8400.../status",
  "message": "Upload URL generated. Use the presignedUrl to upload your file directly to S3."
}
```

### GET /api/v1/documents/{documentId}/status
Poll for document processing status.

**Response:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "status": "EXTRACTED",
  "fileName": "invoice-2024.pdf",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

**Status values:** `PENDING` → `PROCESSING` → `OCR_COMPLETE` → `EXTRACTED` | `FAILED`

---

## Extraction Service (port 8083)

### POST /api/v1/extract
Trigger LLM extraction on cleaned OCR text.

**Request:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "cleanedText": "INVOICE #INV-2024-0892\nBill To: TechCorp Solutions...",
  "fileType": "application/pdf",
  "llmProvider": "openai"
}
```

**Response:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "documentType": "invoice",
  "entities": [
    {"type": "ORG", "value": "TechCorp Solutions", "confidence": 0.97}
  ],
  "dates": [
    {"label": "invoice_date", "value": "January 15, 2024", "normalized": "2024-01-15"}
  ],
  "totals": {"subtotal": 11250.00, "tax": 1200.00, "total": 12450.00, "currency": "USD"},
  "lineItems": [],
  "summary": "Invoice from Acme Supplies to TechCorp for cloud infrastructure.",
  "anomalies": [],
  "llmProvider": "openai",
  "llmModel": "gpt-4o",
  "confidence": 0.962,
  "tokensUsed": 1247,
  "processingTimeMs": 3820,
  "extractedAt": "2024-01-15T10:30:00Z"
}
```

---

## Analytics Service (port 8084)

### GET /api/v1/analytics/dashboard
Returns aggregated metrics for the dashboard.

**Response:**
```json
{
  "totalDocuments": 2847,
  "documentsToday": 143,
  "avgProcessingTimeMs": 4200,
  "avgConfidenceScore": 0.947,
  "documentsWithAnomalies": 38,
  "errorRate": 2.3,
  "documentsByType": {"invoice": 1240, "receipt": 680, "contract": 410},
  "documentsByStatus": {"EXTRACTED": 2650, "PROCESSING": 120, "FAILED": 77},
  "dailyTrend": [
    {"date": "2024-01-09", "count": 380, "avgConfidence": 0.94}
  ],
  "llmModelStats": [
    {"provider": "GPT-4o", "count": 1820, "avgConfidence": 0.962, "avgProcessingTimeMs": 3800}
  ]
}
```

---

## Kafka Topics

| Topic | Producer | Consumer | Payload |
|---|---|---|---|
| `document.uploaded` | upload-service | processing-service | DocumentUploadedEvent |
| `document.ocr.completed` | processing-service | extraction-service | OcrResponse |
| `document.extracted` | extraction-service | analytics-service | ExtractionResult |
| `document.failed` | any service | analytics-service | DocumentUploadedEvent |

---

## Python Pipeline (port 8090)

### POST /process

**Request:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "s3Key": "uploads/550e8400/invoice.pdf",
  "s3Bucket": "ai-doc-intelligence-uploads",
  "contentType": "application/pdf",
  "ocrEngine": "textract"
}
```

**Response:**
```json
{
  "documentId": "550e8400-e29b-41d4-a716-446655440000",
  "rawText": "INVOICE #12345\nBill To: ...",
  "cleanedText": "INVOICE #12345\nBill To: ...",
  "documentType": "invoice",
  "ocrConfidence": 0.984,
  "processingTimeMs": 1240,
  "ocrEngine": "textract",
  "success": true,
  "wordCount": 342,
  "pageCount": 2
}
```
