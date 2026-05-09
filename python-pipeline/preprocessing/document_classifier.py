import re
import logging
from typing import Optional

logger = logging.getLogger(__name__)


class DocumentTypeClassifier:
    """Keyword-heuristic document type classifier."""

    PATTERNS = {
        "invoice": [
            r'\binvoice\b', r'\binv\s*#', r'\bbill\s+to\b', r'\bdue\s+date\b',
            r'\bpayment\s+terms\b', r'\bsubtotal\b', r'\bremit\s+to\b',
        ],
        "receipt": [
            r'\breceipt\b', r'\bthank\s+you\s+for\s+your\s+(purchase|order)\b',
            r'\btransaction\s+id\b', r'\bchange\s+due\b', r'\bpayment\s+received\b',
        ],
        "contract": [
            r'\bagreement\b', r'\bhereinafter\b', r'\bwhereas\b', r'\bparty\b',
            r'\bterms\s+and\s+conditions\b', r'\bsignature\b', r'\bexecution\b',
            r'\bindemnification\b', r'\bliability\b', r'\btermin',
        ],
        "report": [
            r'\bexecutive\s+summary\b', r'\bfindings\b', r'\brecommendations\b',
            r'\banalysis\b', r'\bconclusion\b', r'\bappendix\b', r'\bfigure\s+\d',
        ],
        "letter": [
            r'\bdear\s+\w', r'\bsincerely\b', r'\bregards\b', r'\bto\s+whom\s+it\s+may\s+concern\b',
            r'\byours\s+truly\b', r'\brespectfully\b',
        ],
        "medical": [
            r'\bpatient\b', r'\bdiagnosis\b', r'\bprescription\b', r'\bdosage\b',
            r'\bmedication\b', r'\bphysician\b', r'\bhospital\b', r'\bclinical\b',
        ],
        "financial_statement": [
            r'\bbalance\s+sheet\b', r'\bincome\s+statement\b', r'\bcash\s+flow\b',
            r'\bshareholders\b', r'\bequity\b', r'\bliabilities\b', r'\bassets\b',
            r'\bnet\s+income\b', r'\bearnings\b',
        ],
    }

    def classify(self, text: str, min_confidence: float = 0.3) -> str:
        if not text:
            return "unknown"

        text_lower = text.lower()
        scores: dict[str, float] = {}

        for doc_type, patterns in self.PATTERNS.items():
            matches = sum(
                1 for p in patterns if re.search(p, text_lower)
            )
            scores[doc_type] = matches / len(patterns)

        best_type = max(scores, key=scores.get)
        best_score = scores[best_type]

        logger.debug(f"Classification scores: {scores}")
        logger.info(f"Classified as '{best_type}' with score {best_score:.2f}")

        return best_type if best_score >= min_confidence else "other"

    def classify_with_confidence(self, text: str) -> tuple[str, float]:
        if not text:
            return "unknown", 0.0
        text_lower = text.lower()
        scores: dict[str, float] = {}
        for doc_type, patterns in self.PATTERNS.items():
            matches = sum(1 for p in patterns if re.search(p, text_lower))
            scores[doc_type] = matches / len(patterns)
        best_type = max(scores, key=scores.get)
        return best_type, scores[best_type]
