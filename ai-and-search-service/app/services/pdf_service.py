"""PDF extraction service using PyMuPDF (fitz) as per specification"""
from io import BytesIO
import fitz  # PyMuPDF
from typing import str
import logging

logger = logging.getLogger(__name__)


class PDFExtractionService:
    """Service for extracting text from PDF byte streams using PyMuPDF"""

    @staticmethod
    def extract_text_from_bytes(pdf_bytes: BytesIO) -> str:
        """
        Extract text from PDF byte stream using PyMuPDF.
        NO local file storage - processes directly from memory.

        Args:
            pdf_bytes: PDF content as BytesIO stream

        Returns:
            str: Extracted text from all pages

        Raises:
            ValueError: If PDF is invalid or cannot be processed
        """
        try:
            # Open PDF from memory
            pdf_bytes.seek(0)
            doc = fitz.open(stream=pdf_bytes.read(), filetype="pdf")

            text_content = []

            # Extract text from each page
            for page_num in range(doc.page_count):
                page = doc[page_num]
                text = page.get_text()

                if text.strip():
                    text_content.append(text)

            doc.close()

            # Combine all pages
            full_text = "\n\n".join(text_content)

            if not full_text.strip():
                logger.warning("PDF extraction resulted in empty text")
                raise ValueError("No text could be extracted from PDF")

            logger.info(
                f"Successfully extracted {len(full_text)} characters "
                f"from {doc.page_count} pages"
            )

            return full_text

        except Exception as e:
            logger.error(f"Error extracting text from PDF: {e}")
            raise ValueError(f"Failed to extract text from PDF: {e}")


# Singleton instance
pdf_extraction_service = PDFExtractionService()
