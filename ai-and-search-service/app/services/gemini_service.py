"""Gemini AI service for answering questions based on PDF content"""
import google.generativeai as genai
import logging
from app.config import settings

logger = logging.getLogger(__name__)


class GeminiService:
    """Service for interacting with Google Gemini API"""

    def __init__(self):
        genai.configure(api_key=settings.GEMINI_API_KEY)
        self.model = genai.GenerativeModel(settings.GEMINI_MODEL)
        logger.info(f"GeminiService initialized with model: {settings.GEMINI_MODEL}")

    def answer_question(self, pdf_text: str, query: str) -> str:
        """
        Send extracted PDF text + user query to Gemini and return the answer.

        Args:
            pdf_text: Full text extracted from the PDF document
            query:    The user's question about the PDF content

        Returns:
            str: Gemini's answer

        Raises:
            ValueError: If the model returns an empty or blocked response
            RuntimeError: If the Gemini API call fails
        """
        prompt = (
            "You are a helpful assistant. The user has provided a PDF document "
            "and a question. Answer the question **strictly based on the content "
            "of the document below**. If the answer cannot be found in the document, "
            "say so clearly.\n\n"
            "--- DOCUMENT START ---\n"
            f"{pdf_text}\n"
            "--- DOCUMENT END ---\n\n"
            f"Question: {query}"
        )

        try:
            response = self.model.generate_content(prompt)

            if not response.text:
                raise ValueError("Gemini returned an empty response")

            logger.info(
                f"Gemini answered question ({len(response.text)} chars) "
                f"based on {len(pdf_text)} chars of PDF text"
            )
            return response.text

        except Exception as e:
            logger.error(f"Gemini API error: {e}")
            raise RuntimeError(f"Gemini API call failed: {e}")


# Singleton instance – created lazily so missing API key doesn't crash startup
_gemini_service: GeminiService | None = None


def get_gemini_service() -> GeminiService:
    global _gemini_service
    if _gemini_service is None:
        _gemini_service = GeminiService()
    return _gemini_service
