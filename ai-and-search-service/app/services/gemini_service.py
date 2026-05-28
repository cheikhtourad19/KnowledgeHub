"""Gemini AI service for answering questions based on PDF content"""
import google.generativeai as genai
import json
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

    def summarize_cv(self, cv_text: str) -> dict:
        """
        Summarize a teacher's CV text into a structured JSON object using Gemini.

        Args:
            cv_text: Full text extracted from a CV/resume PDF

        Returns:
            dict: Structured CV summary with keys:
                  teacher_name, specialities, technologies, domains, projects

        Raises:
            ValueError: If the model returns an empty response or invalid JSON
            RuntimeError: If the Gemini API call fails
        """
        prompt = (
            "You are an HR assistant specialized in CV analysis. "
            "Read the CV text below and extract structured information. "
            "Return ONLY valid JSON with no markdown formatting, no code fences, no extra text.\n\n"
            "JSON schema:\n"
            "{\n"
            '  "teacher_name": "Full name of the teacher",\n'
            '  "specialities": ["list of professional specialities/domains of expertise"],\n'
            '  "technologies": ["list of technologies, tools, and frameworks mentioned"],\n'
            '  "domains": ["list of application domains / industries"],\n'
            '  "projects": ["list of notable projects mentioned"]\n'
            "}\n\n"
            "Rules:\n"
            "- teacher_name must be the person's full name as written in the CV.\n"
            "- specialities: professional domains of expertise (e.g. Backend Development, DevOps, AI, Software Architecture).\n"
            "- technologies: tools, frameworks, languages, platforms (e.g. Docker, Spring Boot, Python, PostgreSQL).\n"
            "- domains: industries or fields the person has worked in (e.g. Distributed Systems, E-Health, Web Platforms).\n"
            "- projects: specific project names or descriptions mentioned.\n"
            "- Do NOT invent any information. If a field cannot be determined from the CV, use an empty array [].\n"
            "- If the teacher's name is not found, use null.\n"
            "- Respond ONLY with the JSON object. No preamble, no explanation, no markdown.\n\n"
            "--- CV TEXT ---\n"
            f"{cv_text}\n"
            "--- END CV TEXT ---"
        )

        try:
            response = self.model.generate_content(prompt)

            if not response.text:
                raise ValueError("Gemini returned an empty response")

            raw = response.text.strip()
            # Strip markdown code fences if Gemini wraps the JSON anyway
            if raw.startswith("```"):
                raw = raw.split("\n", 1)[-1]
                raw = raw.rsplit("\n", 1)[0] if raw.endswith("```") else raw
                raw = raw.strip()

            parsed = json.loads(raw)

            logger.info(
                f"Gemini summarized CV into structured JSON "
                f"from {len(cv_text)} chars of text"
            )
            return parsed

        except json.JSONDecodeError as e:
            logger.error(f"Gemini returned invalid JSON: {e}\nRaw response: {raw}")
            raise ValueError(f"Failed to parse Gemini response as JSON: {e}")
        except Exception as e:
            logger.error(f"Gemini CV summarization error: {e}")
            raise RuntimeError(f"Gemini CV summarization failed: {e}")

    def extract_structured_data(self, system_prompt: str, document_text: str) -> str:
        """
        Extract structured data from document text using a strict prompt.

        Args:
            system_prompt: Instruction prompt describing the expected JSON schema
            document_text: Extracted text content from document

        Returns:
            str: Raw model response expected to be JSON

        Raises:
            ValueError: If the model returns an empty response
            RuntimeError: If the Gemini API call fails
        """
        prompt = (
            f"{system_prompt}\n\n"
            "Here is the extracted PDF text:\n\n"
            f"{document_text}"
        )

        try:
            response = self.model.generate_content(prompt)

            if not response.text:
                raise ValueError("Gemini returned an empty response")

            logger.info(
                f"Gemini extracted structured data ({len(response.text)} chars) "
                f"from {len(document_text)} chars of document text"
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
