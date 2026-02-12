"""API router for semantic search endpoints"""
from fastapi import APIRouter, HTTPException, status
from app.models import (
    IndexRequest, IndexResponse,
    SearchRequest, SearchResponse, SearchMatch,
    DeleteResponse,
    HealthResponse
)
from app.services.minio_service import minio_service
from app.services.pdf_service import pdf_extraction_service
from app.services.text_chunking_service import text_chunking_service
from app.services.vector_db_service import vector_db_service
from app.config import settings
import logging

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Semantic Search"])


@router.post("/index", response_model=IndexResponse, status_code=status.HTTP_200_OK)
async def index_document(request: IndexRequest):
    """
    Index a document from MinIO for semantic search.

    Process:
    1. Fetch PDF from MinIO as byte stream (no local storage)
    2. Extract text using PyMuPDF
    3. Split text into chunks (size=1000, overlap=100)
    4. Generate embeddings locally
    5. Upsert to ChromaDB with file_id in metadata
    """
    try:
        logger.info(
            f"Starting indexing for file_id={request.file_id}, "
            f"bucket={request.bucket}, object={request.object_name}"
        )

        # Step 1: Fetch PDF from MinIO (byte stream only, no local storage)
        pdf_bytes = await minio_service.get_pdf_as_bytes(
            bucket=request.bucket,
            object_name=request.object_name
        )

        # Step 2: Extract text using PyMuPDF
        text = pdf_extraction_service.extract_text_from_bytes(pdf_bytes)

        # Step 3: Split text into chunks
        chunks = text_chunking_service.chunk_text(text)

        # Step 4 & 5: Generate embeddings and upsert to ChromaDB
        chunks_indexed = await vector_db_service.index_document(
            file_id=request.file_id,
            chunks=chunks
        )

        return IndexResponse(
            success=True,
            file_id=request.file_id,
            chunks_indexed=chunks_indexed,
            message=f"Successfully indexed document with {chunks_indexed} chunks"
        )

    except ValueError as e:
        logger.error(f"Validation error during indexing: {e}")
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail=str(e)
        )
    except Exception as e:
        logger.error(f"Error during indexing: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Failed to index document: {str(e)}"
        )


@router.post("/search", response_model=SearchResponse, status_code=status.HTTP_200_OK)
async def semantic_search(request: SearchRequest):
    """
    Perform semantic search over indexed documents.

    Process:
    1. Generate embedding for query string
    2. Query ChromaDB for top_k most similar chunks
    3. Extract UNIQUE file_ids from results
    4. Return file_ids with similarity scores
    """
    try:
        logger.info(
            f"Performing search for query: '{request.query[:100]}...', top_k={request.top_k}"
        )

        # Perform semantic search
        matches = await vector_db_service.search(
            query=request.query,
            top_k=request.top_k
        )

        # Convert to response format
        search_matches = [
            SearchMatch(file_id=match["file_id"], score=match["score"])
            for match in matches
        ]

        return SearchResponse(
            query=request.query,
            matches=search_matches,
            total_results=len(search_matches)
        )

    except Exception as e:
        logger.error(f"Error during search: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Search failed: {str(e)}"
        )


@router.delete("/index/{file_id}", response_model=DeleteResponse, status_code=status.HTTP_200_OK)
async def delete_document(file_id: int):
    """
    Delete all vectors associated with a file_id.

    Removes all chunks from ChromaDB where metadata file_id matches.
    """
    try:
        logger.info(f"Deleting all vectors for file_id={file_id}")

        chunks_deleted = await vector_db_service.delete_by_file_id(file_id)

        if chunks_deleted == 0:
            return DeleteResponse(
                success=True,
                file_id=file_id,
                chunks_deleted=0,
                message=f"No vectors found for file_id={file_id}"
            )

        return DeleteResponse(
            success=True,
            file_id=file_id,
            chunks_deleted=chunks_deleted,
            message=f"Successfully deleted {chunks_deleted} chunks"
        )

    except Exception as e:
        logger.error(f"Error during deletion: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Deletion failed: {str(e)}"
        )


@router.get("/health", response_model=HealthResponse, status_code=status.HTTP_200_OK)
async def health_check():
    """
    Health check endpoint to verify service status.

    Checks:
    - Vector database connection and stats
    - MinIO connection
    - Service configuration
    """
    try:
        # Get vector DB stats
        vector_db_stats = vector_db_service.get_stats()

        # Check MinIO connection
        minio_connected = minio_service.check_connection()

        return HealthResponse(
            status="healthy",
            service=settings.SERVICE_NAME,
            version=settings.VERSION,
            vector_db_stats=vector_db_stats,
            minio_connected=minio_connected
        )

    except Exception as e:
        logger.error(f"Health check failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail=f"Service unhealthy: {str(e)}"
        )
