"""Simple language detection service using langdetect"""
from langdetect import detect_langs, DetectorFactory
import logging

DetectorFactory.seed = 0
logger = logging.getLogger(__name__)


def detect_language(text: str) -> str:
    """Detect primary language for a given text. Returns ISO 639-1 code or 'unknown'."""
    if not text or not text.strip():
        return "unknown"

    try:
        langs = detect_langs(text)
        if not langs:
            return "unknown"
        # detect_langs returns ordered probabilities, take the highest-probability language
        primary = langs[0].lang
        logger.debug(f"Detected language='{primary}' (raw={langs})")
        return primary
    except Exception as e:
        logger.debug(f"Language detection failed: {e}")
        return "unknown"
