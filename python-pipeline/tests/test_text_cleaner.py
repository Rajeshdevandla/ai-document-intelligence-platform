import pytest
from preprocessing.text_cleaner import TextCleaner


@pytest.fixture
def cleaner():
    return TextCleaner()


def test_clean_removes_null_bytes(cleaner):
    raw = "Hello\x00 World\x07"
    result = cleaner.clean(raw)
    assert "\x00" not in result
    assert "Hello" in result
    assert "World" in result


def test_clean_collapses_whitespace(cleaner):
    raw = "Invoice   Number:    12345"
    result = cleaner.clean(raw)
    assert "  " not in result
    assert "Invoice" in result


def test_clean_collapses_excessive_newlines(cleaner):
    raw = "Line 1\n\n\n\n\nLine 2"
    result = cleaner.clean(raw)
    assert result.count("\n") <= 2


def test_clean_normalizes_unicode(cleaner):
    raw = "caf\u00e9 na\u00efve"  # café naïve
    result = cleaner.clean(raw)
    assert "caf" in result
    assert len(result) > 0


def test_clean_empty_string(cleaner):
    assert cleaner.clean("") == ""
    assert cleaner.clean(None) == ""


def test_normalize_amounts(cleaner):
    text = "Total: $ 1,234.56"
    result = cleaner.normalize_amounts(text)
    assert "$ " not in result or "$1" in result
