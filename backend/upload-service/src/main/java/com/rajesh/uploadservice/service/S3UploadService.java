package com.rajesh.uploadservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3UploadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiry-seconds:900}")
    private int presignedUrlExpirySeconds;

    public String generatePresignedUploadUrl(String s3Key, String contentType) {
        log.info("Generating presigned URL for key: {}, contentType: {}", s3Key, contentType);

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(r -> r
                .signatureDuration(Duration.ofSeconds(presignedUrlExpirySeconds))
                .putObjectRequest(putObjectRequest));

        String url = presignedRequest.url().toString();
        log.info("Presigned URL generated successfully for key: {}", s3Key);
        return url;
    }

    public String buildS3Key(String documentId, String fileName) {
        String sanitized = fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
        return String.format("uploads/%s/%s", documentId, sanitized);
    }

    public String getBucketName() {
        return bucketName;
    }
}
