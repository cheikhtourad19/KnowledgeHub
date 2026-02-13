"""Configuration settings for the AI and Search Service"""
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    """Application settings"""

    # Service configuration
    SERVICE_NAME: str = "AI and Search Service"
    VERSION: str = "1.0.0"
    DEBUG: bool = False

    # API configuration
    API_HOST: str = "0.0.0.0"
    API_PORT: int = 8000

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

    # PDF processing configuration
    MAX_FILE_SIZE_MB: int = 50

    # Text processing (Optimized for technical PDFs: chunk_size=800, overlap=150)
    MIN_TEXT_LENGTH: int = 10
    CHUNK_SIZE: int = 800
    CHUNK_OVERLAP: int = 150

    class Config:
        env_file = ".env"
        case_sensitive = True


settings = Settings()
