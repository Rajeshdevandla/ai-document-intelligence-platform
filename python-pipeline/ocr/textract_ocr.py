import boto3
import logging
from typing import Tuple

logger = logging.getLogger(__name__)


class TextractOcrEngine:
    """AWS Textract-based OCR engine for high-accuracy document text extraction."""

    def __init__(self, region: str = "us-east-1"):
        self.client = boto3.client("textract", region_name=region)

    def extract_text(
        self, s3_bucket: str, s3_key: str, content_type: str
    ) -> Tuple[str, float, int]:
        """
        Extract text from a document stored in S3 using AWS Textract.

        Returns:
            Tuple of (raw_text, confidence_score, page_count)
        """
        logger.info(f"Running Textract on s3://{s3_bucket}/{s3_key}")

        try:
            response = self.client.detect_document_text(
                Document={"S3Object": {"Bucket": s3_bucket, "Name": s3_key}}
            )

            blocks = response.get("Blocks", [])
            lines = [
                block["Text"]
                for block in blocks
                if block["BlockType"] == "LINE" and "Text" in block
            ]

            confidences = [
                block.get("Confidence", 0.0)
                for block in blocks
                if block["BlockType"] == "LINE"
            ]

            raw_text = "\n".join(lines)
            avg_confidence = sum(confidences) / len(confidences) / 100.0 if confidences else 0.0

            # Count pages from page blocks
            page_count = len(
                [b for b in blocks if b["BlockType"] == "PAGE"]
            ) or 1

            logger.info(
                f"Textract extracted {len(lines)} lines, "
                f"confidence={avg_confidence:.3f}, pages={page_count}"
            )
            return raw_text, avg_confidence, page_count

        except self.client.exceptions.UnsupportedDocumentException:
            logger.error("Textract does not support this document format")
            raise ValueError("Unsupported document format for Textract")
        except Exception as e:
            logger.error(f"Textract extraction failed: {e}")
            raise
