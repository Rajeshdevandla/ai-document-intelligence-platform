package com.docplatform.upload.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UploadRequest {
    @NotBlank(message = "File name is required")
    private String fileName;

    @Pattern(regexp = "application/pdf|image/png|image/jpeg",
             message = "Only PDF, PNG, and JPEG files are allowed")
    private String contentType;

    private Long fileSize;
    private String description;
}
