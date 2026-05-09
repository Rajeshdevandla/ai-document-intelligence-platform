import io
import logging
from typing import Tuple

import boto3
import pytesseract
from PIL import Image
import pdf2image

logger = logging.getLogger(__name__)


class TesseractOcrEngine:
    """Tesseract-based OCR engine — fallback for when Textract is unavailable."""

    def __init__(self, region: str = "us-east-1"):
        self.s3_client = boto3.client("s3", region_name=region)

    def extract_text(
        self, s3_bucket: str, s3_key: str, content_type: str
    ) -> Tuple[str, float, int]:
        """
        Download file from S3 and run Tesseract OCR.

        Returns:
            Tuple of (raw_text, confidence_score, page_count)
        """
        logger.info(f"Running Tesseract on s3://{s3_bucket}/{s3_key}")

        # Download file from S3
        response = self.s3_client.get_object(Bucket=s3_bucket, Key=s3_key)
        file_bytes = response["Body"].read()

        if content_type == "application/pdf":
            return self._process_pdf(file_bytes)
        else:
            return self._process_image(file_bytes)

    def _process_pdf(self, file_bytes: bytes) -> Tuple[str, float, int]:
        images = pdf2image.convert_from_bytes(file_bytes, dpi=300)
        all_text = []
        confidences = []

        for page_img in images:
            text, conf = self._run_tesseract(page_img)
            all_text.append(text)
            if conf > 0:
                confidences.append(conf)

        raw_text = "\n\n--- PAGE BREAK ---\n\n".join(all_text)
        avg_conf = sum(confidences) / len(confidences) / 100.0 if confidences else 0.0
        return raw_text, avg_conf, len(images)

    def _process_image(self, file_bytes: bytes) -> Tuple[str, float, int]:
        image = Image.open(io.BytesIO(file_bytes))
        text, confidence = self._run_tesseract(image)
        return text, confidence / 100.0, 1

    def _run_tesseract(self, image: Image.Image) -> Tuple[str, float]:
        config = "--oem 3 --psm 3"
        text = pytesseract.image_to_string(image, config=config)
        data = pytesseract.image_to_data(image, output_type=pytesseract.Output.DICT, config=config)
        confs = [c for c in data["conf"] if c != -1]
        avg_conf = sum(confs) / len(confs) if confs else 0.0
        return text.strip(), avg_conf
