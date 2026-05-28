"""CV Summary vector service — dedicated ChromaDB collection + cloud embeddings via OpenRouter"""
from typing import List, Dict, Any, Optional
import chromadb
from chromadb.config import Settings as ChromaSettings
import requests
from app.config import settings
import logging

logger = logging.getLogger(__name__)

OPENROUTER_EMBED_URL = "https://openrouter.ai/api/v1/embeddings"
MIN_SIMILARITY = 0.05


class VectorDBCVService:
    def __init__(self):
        self.client = chromadb.PersistentClient(
            path=settings.CHROMA_PERSIST_DIRECTORY,
            settings=ChromaSettings(anonymized_telemetry=False, allow_reset=True),
        )
        self.collection = self.client.get_or_create_collection(
            name=settings.CHROMA_CV_COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"},
        )
        self._model = settings.EMBEDDING_MODEL_V2
        self._api_key = settings.OPENROUTER_API_KEY
        logger.info(
            f"VectorDBCVService initialized — collection='{settings.CHROMA_CV_COLLECTION_NAME}' "
            f"model='{self._model}'"
        )

    def _embed(self, texts: List[str]) -> List[List[float]]:
        response = requests.post(
            OPENROUTER_EMBED_URL,
            headers={
                "Authorization": f"Bearer {self._api_key}",
                "Content-Type": "application/json",
            },
            json={"model": self._model, "input": texts},
            timeout=60,
        )
        response.raise_for_status()
        data = sorted(response.json()["data"], key=lambda x: x["index"])
        return [item["embedding"] for item in data]

    def generate_embedding(self, text: str) -> List[float]:
        return self._embed([text])[0]

    def generate_embeddings(self, texts: List[str]) -> List[List[float]]:
        return self._embed(texts)

    async def index_text(
        self,
        text_id: str,
        chunks: List[str],
        metadata: Optional[Dict[str, Any]] = None,
    ) -> int:
        try:
            embeddings = self.generate_embeddings(chunks)
            base_metadata = dict(metadata) if metadata else {}
            ids = [f"{text_id}_chunk_{i}" for i in range(len(chunks))]
            metadatas = [
                {**base_metadata, "chunk_index": i, "text": chunk[:500]}
                for i, chunk in enumerate(chunks)
            ]
            self.collection.upsert(
                ids=ids, embeddings=embeddings, documents=chunks, metadatas=metadatas
            )
            logger.info(f"Indexed {len(chunks)} chunks for {text_id}")
            return len(chunks)
        except Exception as e:
            logger.error(f"Error indexing CV text: {e}")
            raise

    async def search_summaries(
        self, query: str, top_k: int = 10
    ) -> List[Dict[str, Any]]:
        try:
            query_embedding = self.generate_embedding(query)

            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k * 5,
                where={"document_type": "cv_summary"},
            )

            teacher_chunks: Dict[int, List[float]] = {}
            teacher_meta: Dict[int, str] = {}

            if results["metadatas"] and results["distances"]:
                for metadata, distance in zip(
                    results["metadatas"][0], results["distances"][0]
                ):
                    score = 1.0 - distance
                    if score < MIN_SIMILARITY:
                        continue
                    teacher_id = int(metadata.get("teacher_id", 0))
                    if teacher_id not in teacher_chunks:
                        teacher_chunks[teacher_id] = []
                        teacher_meta[teacher_id] = metadata.get(
                            "teacher_name", f"teacher_{teacher_id}"
                        )
                    teacher_chunks[teacher_id].append(score)

            matches = []
            for teacher_id, chunk_scores in teacher_chunks.items():
                best_score = max(chunk_scores)
                matches.append({
                    "teacher_id": teacher_id,
                    "teacher_name": teacher_meta[teacher_id],
                    "score": float(best_score),
                })

            matches.sort(key=lambda x: x["score"], reverse=True)
            matches = matches[:top_k]

            logger.info(
                f"CV search returned {len(matches)} teachers for query: '{query[:50]}...'"
            )
            return matches

        except Exception as e:
            logger.error(f"Error during CV summary search: {e}")
            raise

    async def delete_by_metadata(self, metadata_filter: Dict[str, Any]) -> int:
        try:
            keys = list(metadata_filter.keys())
            if len(keys) == 1:
                where = metadata_filter
            else:
                where = {"$and": [{k: metadata_filter[k]} for k in keys]}
            results = self.collection.get(where=where)
            if not results["ids"]:
                logger.info(f"No vectors for filter: {metadata_filter}")
                return 0
            self.collection.delete(ids=results["ids"])
            deleted = len(results["ids"])
            logger.info(f"Deleted {deleted} chunks for filter: {metadata_filter}")
            return deleted
        except Exception as e:
            logger.error(f"Error deleting by metadata: {e}")
            raise


vector_db_cv_service = VectorDBCVService()
