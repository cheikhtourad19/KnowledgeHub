"""Pydantic models for API requests and responses"""
from pydantic import BaseModel, Field
from typing import List


# Indexing models
class IndexRequest(BaseModel):
    """Request model for indexing a document"""
    file_id: int = Field(..., description="Unique file ID from PostgreSQL database", gt=0)
    bucket: str = Field(..., description="MinIO bucket name", min_length=1)
    object_name: str = Field(..., description="Object name/path in MinIO bucket", min_length=1)

    class Config:
        json_schema_extra = {
            "example": {
                "file_id": 123,
                "bucket": "academic-documents",
                "object_name": "pfe/2024/report_123.pdf"
            }
        }


class IndexResponse(BaseModel):
    """Response model for indexing operation"""
    success: bool
    file_id: int
    chunks_indexed: int
    message: str


# Search models
class SearchRequest(BaseModel):
    """Request model for semantic search"""
    query: str = Field(..., description="Search query string", min_length=1)
    top_k: int = Field(default=10, description="Number of results to return", gt=0, le=100)

    class Config:
        json_schema_extra = {
            "example": {
                "query": "machine learning algorithms for image classification",
                "top_k": 10
            }
        }


class SearchMatch(BaseModel):
    """Individual search result"""
    file_id: int = Field(..., description="Unique file ID")
    score: float = Field(..., description="Similarity score (0-1)", ge=0, le=1)


class SearchResponse(BaseModel):
    """Response model for search operation"""
    query: str
    matches: List[SearchMatch]
    total_results: int

    class Config:
        json_schema_extra = {
            "example": {
                "query": "machine learning",
                "matches": [
                    {"file_id": 123, "score": 0.89},
                    {"file_id": 456, "score": 0.75}
                ],
                "total_results": 2
            }
        }


# Delete models
class DeleteResponse(BaseModel):
    """Response model for deletion operation"""
    success: bool
    file_id: int
    chunks_deleted: int
    message: str


# Health check
class HealthResponse(BaseModel):
    """Response model for health check"""
    status: str
    service: str
    version: str
    vector_db_stats: dict
    minio_connected: bool
