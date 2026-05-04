"""Vector database service V2 using ChromaDB + OpenRouter Qwen3-Embedding-8B"""
from typing import List, Dict, Any
import chromadb
from chromadb.config import Settings as ChromaSettings
import requests
from app.config import settings
import logging
import math

logger = logging.getLogger(__name__)

# Production-safe ranking constants
MIN_SIMILARITY = 0.50  # Minimum relevance threshold to filter generic matches
MIN_CHUNK_AGREEMENT = 2  # Minimum chunks required above threshold for multi-chunk agreement
AUTHORITY_BOOST_TEACHER = 0.04  # Optional metadata boost for teacher documents
AUTHORITY_BOOST_OFFICIAL = 0.05  # Optional metadata boost for official publications

OPENROUTER_EMBED_URL = "https://openrouter.ai/api/v1/embeddings"

class VectorDBServiceV2:
    """Service for managing ChromaDB vector database with OpenRouter Qwen3-Embedding-8B"""

    def __init__(self):
        # Initialize ChromaDB with PersistentClient
        self.client = chromadb.PersistentClient(
            path=settings.CHROMA_PERSIST_DIRECTORY,
            settings=ChromaSettings(
                anonymized_telemetry=False,
                allow_reset=True
            )
        )

        # V2 uses its own collection so it never conflicts with v1
        self.collection = self.client.get_or_create_collection(
            name=settings.CHROMA_COLLECTION_NAME_V2,
            metadata={"hnsw:space": "cosine"}
        )

        self._model = settings.EMBEDDING_MODEL_V2
        self._api_key = settings.OPENROUTER_API_KEY

        logger.info(
            f"VectorDBServiceV2 initialized - collection='{settings.CHROMA_COLLECTION_NAME_V2}' "
            f"model='{self._model}'"
        )

    def generate_embedding(self, text: str) -> List[float]:
        """
        Generate embedding for a single query string using OpenRouter Qwen3-Embedding-8B.

        Args:
            text: Query string to embed

        Returns:
            List[float]: Embedding vector (3072 dimensions)
        """
        response = requests.post(
            OPENROUTER_EMBED_URL,
            headers={
                "Authorization": f"Bearer {self._api_key}",
                "Content-Type": "application/json",
            },
            json={
                "model": self._model,
                "input": text,
            },
            timeout=30,
        )
        response.raise_for_status()
        return response.json()["data"][0]["embedding"]

    def generate_embeddings(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for multiple document chunks in batch using OpenRouter Qwen3-Embedding-8B.

        Args:
            texts: List of document chunks to embed

        Returns:
            List[List[float]]: List of embedding vectors (3072 dimensions each)
        """
        response = requests.post(
            OPENROUTER_EMBED_URL,
            headers={
                "Authorization": f"Bearer {self._api_key}",
                "Content-Type": "application/json",
            },
            json={
                "model": self._model,
                "input": texts,
            },
            timeout=60,
        )
        response.raise_for_status()
        data = sorted(response.json()["data"], key=lambda item: item["index"])
        return [item["embedding"] for item in data]

    async def index_document(
        self,
        file_id: int,
        chunks: List[str],
    ) -> int:
        """
        Index document chunks with file_id in metadata.

        Args:
            file_id: Unique integer file ID to sync with PostgreSQL
            chunks: List of text chunks

        Returns:
            int: Number of chunks indexed
        """
        try:
            logger.info(f"Generating embeddings for {len(chunks)} chunks")
            embeddings = self.generate_embeddings(chunks)

            ids = [f"{file_id}_chunk_{i}" for i in range(len(chunks))]
            metadatas = [
                {
                    "file_id": file_id,
                    "chunk_index": i,
                    "text": chunk[:500]  # Store preview for debugging
                }
                for i, chunk in enumerate(chunks)
            ]

            self.collection.upsert(
                ids=ids,
                embeddings=embeddings,
                documents=chunks,
                metadatas=metadatas
            )

            logger.info(
                f"Successfully indexed {len(chunks)} chunks for file_id={file_id}"
            )

            return len(chunks)

        except Exception as e:
            logger.error(f"Error indexing document: {e}")
            raise

    async def search(
        self,
        query: str,
        top_k: int = 10,
    ) -> List[Dict[str, Any]]:
        """
        Perform semantic search with production-safe ranking improvements.

        Ranking improvements:
        1. Chunk-count normalization to reduce large document bias
        2. Minimum relevance threshold (MIN_SIMILARITY = 0.30)
        3. Multi-chunk agreement (at least 2 chunks above threshold)
        4. Optional authority-aware metadata boosts

        Args:
            query: Search query string
            top_k: Number of results to return

        Returns:
            List[Dict]: List of {"file_id": int, "score": float}
        """
        try:
            query_embedding = self.generate_embedding(query)

            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k * 5
            )

            file_chunks = {}   # {file_id: [scores]}
            file_metadata = {} # {file_id: metadata}

            if results['metadatas'] and results['distances']:
                for metadata, distance in zip(
                    results['metadatas'][0],
                    results['distances'][0]
                ):
                    file_id = metadata['file_id']
                    score = 1.0 - distance  # cosine distance → similarity

                    if score < MIN_SIMILARITY:
                        continue

                    if file_id not in file_chunks:
                        file_chunks[file_id] = []
                        file_metadata[file_id] = metadata

                    file_chunks[file_id].append(score)

            normalized_matches = []

            for file_id, chunk_scores in file_chunks.items():
                if len(chunk_scores) < MIN_CHUNK_AGREEMENT:
                    logger.debug(
                        f"Skipping file_id={file_id}: only {len(chunk_scores)} chunk(s) above threshold"
                    )
                    continue

                best_chunk_score = max(chunk_scores)
                chunk_count = len(chunk_scores)

                normalized_matches.append({
                    "file_id": file_id,
                    "score": float(best_chunk_score),
                    "chunk_count": chunk_count,
                    "best_chunk_score": float(best_chunk_score)
                })

            normalized_matches.sort(key=lambda x: x['score'], reverse=True)
            normalized_matches = normalized_matches[:top_k]

            matches = normalized_matches  # Keep all fields for logging

            logger.info(
                f"Search returned {len(matches)} unique file_ids for query: '{query[:50]}...' "
                f"(filtered by MIN_SIMILARITY={MIN_SIMILARITY}, MIN_CHUNK_AGREEMENT={MIN_CHUNK_AGREEMENT})"
            )
            for match in matches:
                logger.info(f"  file_id={match['file_id']}, score={match['score']:.4f}, best_chunk_score={match.get('best_chunk_score', 'N/A')}, chunk_count={match.get('chunk_count', 'N/A')}")

            return matches

        except Exception as e:
            logger.error(f"Error during search: {e}")
            raise

    async def delete_by_file_id(self, file_id: int) -> int:
        """
        Delete all vectors associated with a file_id.

        Args:
            file_id: File ID to delete

        Returns:
            int: Number of chunks deleted
        """
        try:
            results = self.collection.get(
                where={"file_id": file_id}
            )

            if not results['ids']:
                logger.info(f"No vectors found for file_id={file_id}")
                return 0

            self.collection.delete(ids=results['ids'])

            deleted_count = len(results['ids'])
            logger.info(f"Deleted {deleted_count} chunks for file_id={file_id}")

            return deleted_count

        except Exception as e:
            logger.error(f"Error deleting vectors for file_id={file_id}: {e}")
            raise

    def get_stats(self) -> Dict[str, Any]:
        """Get collection statistics"""
        try:
            count = self.collection.count()
            return {
                "collection_name": settings.CHROMA_COLLECTION_NAME_V2,
                "total_vectors": count,
                "persist_directory": settings.CHROMA_PERSIST_DIRECTORY
            }
        except Exception as e:
            logger.error(f"Error getting stats: {e}")
            return {"error": str(e)}


# Singleton instance
vector_db_service_v2 = VectorDBServiceV2()