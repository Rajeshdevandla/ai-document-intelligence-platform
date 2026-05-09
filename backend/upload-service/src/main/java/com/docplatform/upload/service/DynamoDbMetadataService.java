package com.docplatform.upload.service;

import com.docplatform.upload.model.Document;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DynamoDbMetadataService {

    private final DynamoDbClient dynamoDbClient;

    @Value("${aws.dynamodb.table:document-metadata}")
    private String tableName;

    public void saveDocument(Document document) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("documentId", attr(document.getDocumentId()));
        item.put("fileName",   attr(document.getFileName()));
        item.put("fileType",   attr(document.getFileType()));
        item.put("fileSize",   AttributeValue.fromN(String.valueOf(document.getFileSize())));
        item.put("s3Key",      attr(document.getS3Key()));
        item.put("s3Bucket",   attr(document.getS3Bucket()));
        item.put("status",     attr(document.getStatus().name()));
        item.put("uploadedBy", attr(document.getUploadedBy()));
        item.put("uploadedAt", attr(document.getUploadedAt().toString()));
        item.put("updatedAt",  attr(Instant.now().toString()));

        dynamoDbClient.putItem(PutItemRequest.builder().tableName(tableName).item(item).build());
        log.debug("Saved document metadata to DynamoDB: documentId={}", document.getDocumentId());
    }

    public Document getDocument(String documentId) {
        GetItemResponse response = dynamoDbClient.getItem(GetItemRequest.builder()
                .tableName(tableName)
                .key(Map.of("documentId", attr(documentId)))
                .build());

        if (!response.hasItem()) {
            throw new RuntimeException("Document not found: " + documentId);
        }

        Map<String, AttributeValue> item = response.item();
        return Document.builder()
                .documentId(str(item, "documentId"))
                .fileName(str(item, "fileName"))
                .fileType(str(item, "fileType"))
                .s3Key(str(item, "s3Key"))
                .s3Bucket(str(item, "s3Bucket"))
                .status(Document.DocumentStatus.valueOf(str(item, "status")))
                .uploadedBy(str(item, "uploadedBy"))
                .uploadedAt(Instant.parse(str(item, "uploadedAt")))
                .build();
    }

    private AttributeValue attr(String val) {
        return AttributeValue.fromS(val != null ? val : "");
    }

    private String str(Map<String, AttributeValue> item, String key) {
        return item.containsKey(key) ? item.get(key).s() : null;
    }
}
