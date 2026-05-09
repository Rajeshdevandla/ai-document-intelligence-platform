from pydantic import BaseModel, Field
from typing import Optional
from enum import Enum


class OcrEngine(str, Enum):
    TEXTRACT = "textract"
    TESSERACT = "tesseract"


class ProcessRequest(BaseModel):
    documentId: str = Field(..., description="Unique document identifier")
    s3Key: str = Field(..., description="S3 object key")
    s3Bucket: str = Field(..., description="S3 bucket name")
    contentType: str = Field(..., description="MIME type of the document")
    ocrEngine: OcrEngine = Field(OcrEngine.TEXTRACT, description="OCR engine to use")


class ProcessResponse(BaseModel):
    documentId: str
    rawText: str = ""
    cleanedText: str = ""
    documentType: str = "unknown"
    ocrConfidence: float = 0.0
    processingTimeMs: int = 0
    ocrEngine: str = "textract"
    success: bool = True
    errorMessage: Optional[str] = None
    wordCount: int = 0
    pageCount: int = 1
