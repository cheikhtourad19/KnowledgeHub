"""Router for internship convention PDF pre-fill extraction."""
import json
import logging
from io import BytesIO
from typing import Literal

from fastapi import APIRouter, File, HTTPException, UploadFile, status
from pydantic import BaseModel

from app.config import settings
from app.services.gemini_service import get_gemini_service
from app.services.pdf_service import pdf_extraction_service

logger = logging.getLogger(__name__)

router = APIRouter(prefix="/api/internship", tags=["Internship"])

PROMPT = """You are a document parsing assistant. You will receive raw text extracted from an internship convention PDF form (ESPRIM, Tunisia). 

Your task is to extract the following fields and return ONLY a valid JSON object — no preamble, no markdown, no explanation.

Fields to extract:
- studentName: full name of the student
- studentIdentifiant: student ID/identifiant number
- studentFiliere: field of study (filière)
- studentCin: CIN (national ID number)
- studentEmail: student email
- studentPhone: student phone number
- studentAddress: student address
- entrepriseName: company name
- entrepriseDomaine: company domain/sector
- entrepriseAddress: company address
- entreprisePhone: company phone
- entrepriseEmail: company email
- entrepriseSiteWeb: company website
- entrepriseEncadrantName: supervisor full name
- entrepriseEncadrantFonction: supervisor job title
- entrepriseEncadrantService: supervisor department/service
- entrepriseEncadrantEmail: supervisor email
- pfeTitle: internship subject/title (sujet du stage)
- dateDebut: start date of internship in ISO format YYYY-MM-DD, or null if not found
- dateFin: end date of internship in ISO format YYYY-MM-DD, or null if not found
- stageType: either "ouvrier", "technicien", or null

Rules:
- If a field is blank, missing, or unclear, set it to null.
- Never invent or guess values.
- Return ONLY the JSON object, nothing else."""


class ConventionExtractResponse(BaseModel):
    studentName: str | None = None
    studentIdentifiant: str | None = None
    studentFiliere: str | None = None
    studentCin: str | None = None
    studentEmail: str | None = None
    studentPhone: str | None = None
    studentAddress: str | None = None
    entrepriseName: str | None = None
    entrepriseDomaine: str | None = None
    entrepriseAddress: str | None = None
    entreprisePhone: str | None = None
    entrepriseEmail: str | None = None
    entrepriseSiteWeb: str | None = None
    entrepriseEncadrantName: str | None = None
    entrepriseEncadrantFonction: str | None = None
    entrepriseEncadrantService: str | None = None
    entrepriseEncadrantEmail: str | None = None
    pfeTitle: str | None = None
    dateDebut: str | None = None
    dateFin: str | None = None
    stageType: Literal["ouvrier", "technicien"] | None = None


def _clean_json_response(raw_response: str) -> str:
    cleaned = raw_response.strip()

    if cleaned.startswith("```"):
        parts = cleaned.split("```")
        if len(parts) >= 2:
            cleaned = parts[1].strip()
            if cleaned.lower().startswith("json"):
                cleaned = cleaned[4:].strip()

    return cleaned


@router.post(
    "/extract-convention",
    response_model=ConventionExtractResponse,
    status_code=status.HTTP_200_OK,
)
async def extract_convention(file: UploadFile = File(...)):
    max_bytes = settings.MAX_FILE_SIZE_MB * 1024 * 1024

    if file.content_type != "application/pdf":
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Only PDF files are accepted.",
        )

    file_bytes = await file.read()

    if not file_bytes:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Uploaded file is empty.",
        )

    if len(file_bytes) > max_bytes:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"File exceeds maximum size of {settings.MAX_FILE_SIZE_MB} MB.",
        )

    if not file_bytes.startswith(b"%PDF"):
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Invalid PDF file.",
        )

    try:
        raw_text = pdf_extraction_service.extract_text_from_bytes(BytesIO(file_bytes))
    except ValueError as e:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail=f"Text extraction failed: {e}",
        )

    if not raw_text or len(raw_text.strip()) < 20:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Could not extract readable text from PDF.",
        )

    try:
        gemini = get_gemini_service()
        raw_json = gemini.extract_structured_data(
            system_prompt=PROMPT,
            document_text=raw_text,
        )
        parsed = json.loads(_clean_json_response(raw_json))
        validated = ConventionExtractResponse.model_validate(parsed)
    except json.JSONDecodeError:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Gemini returned invalid JSON.",
        )
    except RuntimeError as e:
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Gemini call failed: {e}",
        )
    except Exception as e:
        logger.error(f"Convention extraction failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail="Gemini returned unexpected JSON structure.",
        )

    return validated
