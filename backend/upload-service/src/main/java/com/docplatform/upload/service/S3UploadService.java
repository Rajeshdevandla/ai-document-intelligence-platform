package com.docplatform.upload.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;
import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3UploadService {

    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiry-minutes:15}")
    private int presignedUrlExpiryMinutes;

    /**
     * Generates a presigned PUT URL so the client can upload directly to S3
     * without routing the binary through our service.
     */
    public PresignedUploadResult generatePresignedUrl(String fileName, String contentType) {
        String s3Key = "uploads/" + UUID.randomUUID() + "/" + sanitize(fileName);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(presignedUrlExpiryMinutes))
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        log.info("Generated presigned URL for s3Key={} expiresIn={}min", s3Key, presignedUrlExpiryMinutes);

        return PresignedUploadResult.builder()
                .presignedUrl(presignedRequest.url().toString())
                .s3Key(s3Key)
                .expiresInSeconds((long) presignedUrlExpiryMinutes * 60)
                .build();
    }

    private String sanitize(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    @lombok.Builder
    @lombok.Data
    public static class PresignedUploadResult {
        private String presignedUrl;
        private String s3Key;
        private Long expiresInSeconds;
    }
}
