package com.docplatform.extraction.llm;

import com.docplatform.extraction.model.ExtractionResult;

public interface LlmProvider {
    ExtractionResult extract(String documentId, String text);
    String getProviderName();
}
