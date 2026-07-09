"""API router for teacher search via CV summaries (cloud embeddings)"""
from fastapi import APIRouter, Body, HTTPException, status
from app.models import (
    TeacherMatch, SearchTeachersResponse,
)
from app.services.vector_db_cv_service import vector_db_cv_service
import logging
from typing import Dict, Any, List

logger = logging.getLogger(__name__)

router = APIRouter(tags=["Semantic Search"])




@router.post(
    "/search-teachers",
    response_model=SearchTeachersResponse,
    status_code=status.HTTP_200_OK,
    summary="Find matching teachers for a PFE/project by searching CV summaries",
    description="Takes a list of PFE/project objects, builds a search query from their fields (title, description, keywords, domain), and finds teachers with matching CV summaries via semantic search.",
)
async def search_teachers(pfe_list: List[Dict[str, Any]] = Body(...)):
    try:
        query_parts = []
        for pfe in pfe_list:
            for field in ["pfe_title", "pfe_description", "mot_cles", "entreprise_domaine", "entreprise_name"]:
                val = pfe.get(field)
                if val:
                    query_parts.append(str(val))

        query = " ".join(query_parts)
        logger.info(f"Teacher search from {len(pfe_list)} PFE(s), query='{query[:100]}...'")

        matches = await vector_db_cv_service.search_summaries(query=query, top_k=10)

        teacher_matches = [
            TeacherMatch(teacher_id=m["teacher_id"], teacher_name=m["teacher_name"], score=m["score"])
            for m in matches
        ]

        return SearchTeachersResponse(
            query=query,
            matches=teacher_matches,
            total_results=len(teacher_matches),
        )

    except Exception as e:
        logger.error(f"Error during teacher search: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Teacher search failed: {str(e)}"
        )

