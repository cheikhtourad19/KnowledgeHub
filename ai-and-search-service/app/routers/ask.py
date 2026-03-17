"""Router for PDF Q&A endpoint powered by Gemini (file_id-based)"""
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel, Field
from app.services.minio_service import minio_service
from app.services.pdf_service import pdf_extraction_service
from app.services.mongo_service import mongo_text_cache
from app.services.gemini_service import get_gemini_service
import logging
from io import BytesIO

logger = logging.getLogger(__name__)

router = APIRouter(tags=["PDF Q&A"])


# ── Request / Response models ──────────────────────────────────────────────────

class AskRequest(BaseModel):
    """Request body for the /ask endpoint"""
    file_id: int = Field(..., gt=0, description="Unique file ID (same as used in /index)")
    bucket: str = Field(..., min_length=1, description="MinIO bucket name (used as fallback if text not cached)")
    object_name: str = Field(..., min_length=1, description="Object path in MinIO (used as fallback if text not cached)")
    query: str = Field(..., min_length=1, description="Question about the document")

    class Config:
        json_schema_extra = {
            "example": {
                "file_id": 123,
                "bucket": "academic-documents",
                "object_name": "pfe/2024/report_123.pdf",
                "query": "What is the main contribution of this paper?"
            }
        }


class AskResponse(BaseModel):
    """Response body for the /ask endpoint"""
    file_id: int
    query: str
    answer: str
    source: str = Field(description="'cache' if text was already stored, 'extracted' if fetched from MinIO now")
    characters_in_document: int


# ── Endpoint ───────────────────────────────────────────────────────────────────

@router.post(
    "/ask",
    response_model=AskResponse,
    status_code=status.HTTP_200_OK,
    summary="Ask a question about an indexed document",
    description=(
        "Provide a `file_id` and a `query`. "
        "The service first looks up the cached extracted text in MongoDB. "
        "If not found it fetches the PDF from MinIO, extracts the text, "
        "saves it to MongoDB for future use, then sends everything to Gemini."
    ),
)
async def ask_about_document(request: AskRequest):
    """
    **Flow:**
    1. Look up `file_id` in MongoDB → get cached extracted text  
    2. If cache miss → fetch PDF from MinIO → extract text → save to MongoDB  
    3. Send (extracted text + query) to Gemini  
    4. Return the answer
    """

    # ── Step 1: Try MongoDB cache ──────────────────────────────────────────────
    pdf_text = await mongo_text_cache.get_text(request.file_id)
    source = "cache"

    # ── Step 2: Cache miss → fetch from MinIO and extract ─────────────────────
    if pdf_text is None:
        logger.info(
            f"Cache miss for file_id={request.file_id}. "
            f"Fetching from MinIO: {request.bucket}/{request.object_name}"
        )
        try:
            pdf_bytes: BytesIO = await minio_service.get_pdf_as_bytes(
                bucket=request.bucket,
                object_name=request.object_name,
            )
        except Exception as e:
            raise HTTPException(
                status_code=status.HTTP_404_NOT_FOUND,
                detail=f"Could not fetch PDF from MinIO for file_id={request.file_id}: {e}",
            )

        try:
            pdf_text = pdf_extraction_service.extract_text_from_bytes(pdf_bytes)
        except ValueError as e:
            raise HTTPException(
                status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
                detail=f"Could not extract text from PDF: {e}",
            )

        # Save to MongoDB so next /ask call hits the cache
        await mongo_text_cache.save_text(
            file_id=request.file_id,
            extracted_text=pdf_text,
        )
        source = "extracted"

    logger.info(
        f"file_id={request.file_id} | source={source} | "
        f"text_len={len(pdf_text)} | query='{request.query[:80]}'"
    )

    # ── Step 3: Ask Gemini ─────────────────────────────────────────────────────
    try:
        gemini = get_gemini_service()
        answer = gemini.answer_question(pdf_text=pdf_text, query=request.query)
    except RuntimeError as e:
        raise HTTPException(
            status_code=status.HTTP_502_BAD_GATEWAY,
            detail=str(e),
        )

    return AskResponse(
        file_id=request.file_id,
        query=request.query,
        answer=answer,
        source=source,
        characters_in_document=len(pdf_text),
    )
