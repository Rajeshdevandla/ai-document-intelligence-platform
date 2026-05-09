package com.rajesh.uploadservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class UploadRequest {

    @NotBlank(message = "File name is required")
    private String fileName;

    @NotBlank(message = "Content type is required")
    @Pattern(
        regexp = "application/pdf|image/png|image/jpeg|image/jpg",
        message = "Only PDF, PNG, and JPEG files are supported"
    )
    private String contentType;

    @Positive(message = "File size must be positive")
    private Long fileSize;

    private String uploadedBy;
}
