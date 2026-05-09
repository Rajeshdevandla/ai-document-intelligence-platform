package com.rajesh.extractionservice.provider;

import com.rajesh.extractionservice.model.ExtractionResult;

public interface LlmProvider {
    ExtractionResult extract(String documentId, String cleanedText, String fileType);
    String getProviderName();
    String getModelName();
}
