package com.rajesh.uploadservice.service;

import com.rajesh.uploadservice.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DynamoDbMetadataService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table-name:document-metadata}")
    private String tableName;

    public void saveDocument(Document document) {
        log.info("Saving document metadata: {}", document.getDocumentId());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("documentId",   AttributeValue.fromS(document.getDocumentId()));
        item.put("fileName",     AttributeValue.fromS(document.getFileName()));
        item.put("contentType",  AttributeValue.fromS(document.getContentType()));
        item.put("fileSize",     AttributeValue.fromN(String.valueOf(document.getFileSize())));
        item.put("s3Key",        AttributeValue.fromS(document.getS3Key()));
        item.put("s3Bucket",     AttributeValue.fromS(document.getS3Bucket()));
        item.put("status",       AttributeValue.fromS(document.getStatus().name()));
        item.put("uploadedBy",   AttributeValue.fromS(
                document.getUploadedBy() != null ? document.getUploadedBy() : "anonymous"));
        item.put("createdAt",    AttributeValue.fromS(document.getCreatedAt().toString()));
        item.put("updatedAt",    AttributeValue.fromS(document.getUpdatedAt().toString()));

        PutItemRequest request = PutItemRequest.builder()
                .tableName(tableName)
                .item(item)
                .build();

        dynamoDbClient.putItem(request);
        log.info("Document metadata saved successfully: {}", document.getDocumentId());
    }

    public Optional<Document> findById(String documentId) {
        log.info("Fetching document metadata: {}", documentId);

        GetItemRequest request = GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("documentId", AttributeValue.fromS(documentId)))
                .build();

        GetItemResponse response = dynamoDbClient.getItem(request);

        if (!response.hasItem()) {
            return Optional.empty();
        }

        Map<String, AttributeValue> item = response.item();
        Document doc = Document.builder()
                .documentId(item.get("documentId").s())
                .fileName(item.get("fileName").s())
                .contentType(item.get("contentType").s())
                .fileSize(Long.valueOf(item.get("fileSize").n()))
                .s3Key(item.get("s3Key").s())
                .s3Bucket(item.get("s3Bucket").s())
                .status(Document.DocumentStatus.valueOf(item.get("status").s()))
                .uploadedBy(item.get("uploadedBy").s())
                .createdAt(Instant.parse(item.get("createdAt").s()))
                .updatedAt(Instant.parse(item.get("updatedAt").s()))
                .build();

        return Optional.of(doc);
    }

    public void updateStatus(String documentId, Document.DocumentStatus status, String notes) {
        Map<String, AttributeValue> key = Map.of(
                "documentId", AttributeValue.fromS(documentId));

        Map<String, AttributeValueUpdate> updates = new HashMap<>();
        updates.put("status", AttributeValueUpdate.builder()
                .value(AttributeValue.fromS(status.name()))
                .action(AttributeAction.PUT)
                .build());
        updates.put("updatedAt", AttributeValueUpdate.builder()
                .value(AttributeValue.fromS(Instant.now().toString()))
                .action(AttributeAction.PUT)
                .build());
        if (notes != null) {
            updates.put("processingNotes", AttributeValueUpdate.builder()
                    .value(AttributeValue.fromS(notes))
                    .action(AttributeAction.PUT)
                    .build());
        }

        dynamoDbClient.updateItem(UpdateItemRequest.builder()
                .tableName(tableName)
                .key(key)
                .attributeUpdates(updates)
                .build());
    }
}
