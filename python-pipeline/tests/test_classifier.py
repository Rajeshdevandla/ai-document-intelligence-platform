import pytest
from preprocessing.document_classifier import DocumentTypeClassifier


@pytest.fixture
def classifier():
    return DocumentTypeClassifier()


def test_classify_invoice(classifier):
    text = "INVOICE #12345\nBill To: Acme Corp\nDue Date: 2024-01-31\nSubtotal: $500.00\nPayment Terms: Net 30"
    result = classifier.classify(text)
    assert result == "invoice"


def test_classify_contract(classifier):
    text = "This Agreement is entered into whereas both parties hereinafter agree to the terms and conditions. Indemnification clause applies. Signature required."
    result = classifier.classify(text)
    assert result == "contract"


def test_classify_letter(classifier):
    text = "Dear Mr. Johnson,\nThank you for your inquiry. Please find the details below.\nSincerely,\nRajesh Kumar"
    result = classifier.classify(text)
    assert result == "letter"


def test_classify_unknown_returns_other(classifier):
    text = "aaaa bbbb cccc dddd"
    result = classifier.classify(text)
    assert result == "other"


def test_classify_with_confidence(classifier):
    text = "INVOICE #001\nBill To: Client\nDue Date: 2024-12-01\nSubtotal: $100"
    doc_type, confidence = classifier.classify_with_confidence(text)
    assert doc_type == "invoice"
    assert confidence > 0.3
