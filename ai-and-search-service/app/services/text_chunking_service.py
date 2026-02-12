"""Text chunking service with RecursiveCharacterTextSplitter"""
from typing import List
from langchain.text_splitter import RecursiveCharacterTextSplitter
from app.config import settings
import logging

logger = logging.getLogger(__name__)


class TextChunkingService:
    """Service for splitting text into chunks as per specification"""

    def __init__(self):
        # Initialize RecursiveCharacterTextSplitter as per specification
        # chunk_size=1000, overlap=100
        self.text_splitter = RecursiveCharacterTextSplitter(
            chunk_size=settings.CHUNK_SIZE,
            chunk_overlap=settings.CHUNK_OVERLAP,
            length_function=len,
            separators=["\n\n", "\n", ". ", " ", ""],
        )
        logger.info(
            f"Text chunker initialized with chunk_size={settings.CHUNK_SIZE}, "
            f"overlap={settings.CHUNK_OVERLAP}"
        )

    def chunk_text(self, text: str) -> List[str]:
        """
        Split text into chunks using RecursiveCharacterTextSplitter.

        Args:
            text: Full text content to be chunked

        Returns:
            List[str]: List of text chunks

        Raises:
            ValueError: If text is too short or empty
        """
        if not text or len(text.strip()) < settings.MIN_TEXT_LENGTH:
            raise ValueError(
                f"Text is too short (minimum length: {settings.MIN_TEXT_LENGTH})"
            )

        try:
            # Split text into chunks
            chunks = self.text_splitter.split_text(text)

            # Filter out empty or too-short chunks
            valid_chunks = [
                chunk.strip()
                for chunk in chunks
                if len(chunk.strip()) >= settings.MIN_TEXT_LENGTH
            ]

            logger.info(
                f"Split text into {len(valid_chunks)} chunks "
                f"(original length: {len(text)} characters)"
            )

            return valid_chunks

        except Exception as e:
            logger.error(f"Error chunking text: {e}")
            raise ValueError(f"Failed to chunk text: {e}")


# Singleton instance
text_chunking_service = TextChunkingService()
