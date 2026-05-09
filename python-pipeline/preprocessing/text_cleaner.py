import re
import unicodedata
import logging

logger = logging.getLogger(__name__)


class TextCleaner:
    """Cleans and normalizes raw OCR text for LLM extraction."""

    # Common OCR misread characters
    OCR_CORRECTIONS = {
        r'\bl\b': '1',   # lowercase L mistaken for 1
        r'\bO\b': '0',   # uppercase O mistaken for 0
        r'\|': 'I',      # pipe as uppercase I
        r'\.{3,}': '...',# collapse multiple dots
    }

    def clean(self, raw_text: str) -> str:
        if not raw_text:
            return ""

        text = raw_text

        # Normalize unicode characters
        text = unicodedata.normalize("NFKC", text)

        # Remove null bytes and control characters (keep newlines/tabs)
        text = re.sub(r'[\x00-\x08\x0b-\x0c\x0e-\x1f\x7f]', '', text)

        # Fix common OCR artifacts
        text = re.sub(r'([a-z])([A-Z])', r'\1 \2', text)  # missing space between words
        text = re.sub(r'(\d)([A-Za-z])', r'\1 \2', text)  # number + letter
        text = re.sub(r'([A-Za-z])(\d)', r'\1 \2', text)  # letter + number

        # Collapse multiple spaces (but preserve newlines)
        text = re.sub(r'[ \t]+', ' ', text)

        # Collapse more than 2 consecutive newlines
        text = re.sub(r'\n{3,}', '\n\n', text)

        # Strip lines that are just punctuation/noise
        lines = text.split('\n')
        cleaned_lines = [
            line.strip()
            for line in lines
            if len(re.sub(r'[^a-zA-Z0-9]', '', line.strip())) > 1
        ]

        result = '\n'.join(cleaned_lines).strip()
        logger.debug(f"Text cleaned: {len(raw_text)} → {len(result)} chars")
        return result

    def extract_tables(self, text: str) -> list[str]:
        """Identify potential table sections in the text."""
        table_pattern = re.compile(
            r'((?:.*\|.*\n){2,})',
            re.MULTILINE
        )
        return table_pattern.findall(text)

    def normalize_amounts(self, text: str) -> str:
        """Standardize currency amounts."""
        # $1,234.56 or USD 1234.56
        text = re.sub(r'\$\s*(\d)', r'$\1', text)
        text = re.sub(r'USD\s*(\d)', r'USD \1', text)
        return text
