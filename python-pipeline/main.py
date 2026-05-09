import time
import logging
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from models import ProcessRequest, ProcessResponse
from ocr.textract_ocr import TextractOcrEngine
from ocr.tesseract_ocr import TesseractOcrEngine
from preprocessing.text_cleaner import TextCleaner
from preprocessing.document_classifier import DocumentTypeClassifier

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="DocIQ Python Pipeline",
    description="OCR and text preprocessing pipeline for AI Document Intelligence Platform",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

text_cleaner = TextCleaner()
classifier = DocumentTypeClassifier()


@app.get("/health")
async def health():
    return {"status": "UP", "service": "python-pipeline", "version": "1.0.0"}


@app.post("/process", response_model=ProcessResponse)
async def process_document(request: ProcessRequest):
    start_time = time.time()
    logger.info(f"Processing document: {request.documentId}, engine: {request.ocrEngine}")

    try:
        # Step 1: OCR
        if request.ocrEngine.value == "textract":
            engine = TextractOcrEngine()
        else:
            engine = TesseractOcrEngine()

        raw_text, confidence, page_count = engine.extract_text(
            request.s3Bucket, request.s3Key, request.contentType
        )

        # Step 2: Clean text
        cleaned_text = text_cleaner.clean(raw_text)

        # Step 3: Classify document
        doc_type = classifier.classify(cleaned_text)

        processing_ms = int((time.time() - start_time) * 1000)
        word_count = len(cleaned_text.split()) if cleaned_text else 0

        logger.info(
            f"Document {request.documentId} processed: type={doc_type}, "
            f"confidence={confidence:.2f}, words={word_count}, time={processing_ms}ms"
        )

        return ProcessResponse(
            documentId=request.documentId,
            rawText=raw_text,
            cleanedText=cleaned_text,
            documentType=doc_type,
            ocrConfidence=confidence,
            processingTimeMs=processing_ms,
            ocrEngine=request.ocrEngine.value,
            success=True,
            wordCount=word_count,
            pageCount=page_count,
        )

    except Exception as e:
        logger.error(f"Processing failed for {request.documentId}: {e}")
        processing_ms = int((time.time() - start_time) * 1000)
        return ProcessResponse(
            documentId=request.documentId,
            success=False,
            errorMessage=str(e),
            processingTimeMs=processing_ms,
            ocrEngine=request.ocrEngine.value,
        )


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8090, log_level="info")
