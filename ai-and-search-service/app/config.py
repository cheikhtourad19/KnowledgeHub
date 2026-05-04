"""Configuration settings for the AI and Search Service"""
from pydantic_settings import BaseSettings, SettingsConfigDict
from pydantic import Field


class Settings(BaseSettings):
    """Application settings"""

    # Service configuration
    SERVICE_NAME: str = "AI and Search Service"
    VERSION: str = "1.0.0"
    DEBUG: bool = False

    # API configuration
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8000

    # MongoDB configuration (text cache)
    MONGO_URI: str = "mongodb://localhost:27017"
    MONGO_DB_NAME: str = "knowledge_hub"
    MONGO_COLLECTION_NAME: str = "pdf_text_cache"

    # MinIO configuration (for fetching PDFs)
    MINIO_ENDPOINT: str = "localhost:9000"
    MINIO_ACCESS_KEY: str = "minioadmin"
    MINIO_SECRET_KEY: str = "minioadmin"
    MINIO_SECURE: bool = False

    # Vector database configuration
    CHROMA_PERSIST_DIRECTORY: str = "./chroma_db"
    CHROMA_COLLECTION_NAME: str = "documents"

    # Embedding model configuration (Local execution as per specification)
    EMBEDDING_MODEL: str = "sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2"

    # V2 multilingual embedding (OpenRouter - Qwen3-Embedding-8B)
    EMBEDDING_MODEL_V2: str = "Qwen/Qwen3-Embedding-8B"
    CHROMA_COLLECTION_NAME_V2: str = "documents_v2"

    OPENROUTER_API_KEY: str = ""

    # Google Gemini configuration
    GEMINI_API_KEY: str = Field(..., description="Google Gemini API key")
    GEMINI_MODEL: str = "gemini-2.5-flash"

    # PDF processing configuration
    MAX_FILE_SIZE_MB: int = 50

    # Text processing (Optimized for technical PDFs: chunk_size=800, overlap=150)
    MIN_TEXT_LENGTH: int = 10
    CHUNK_SIZE: int = 800
    CHUNK_OVERLAP: int = 150

    model_config = SettingsConfigDict(env_file=".env", case_sensitive=True)


settings = Settings()
