"""Vector database service using ChromaDB with persistent storage"""
from typing import List, Dict, Any, Optional
import chromadb
from chromadb.config import Settings as ChromaSettings
from sentence_transformers import SentenceTransformer
from app.config import settings
import logging
import math

logger = logging.getLogger(__name__)

# Production-safe ranking constants
MIN_SIMILARITY = 0.30  # Minimum relevance threshold to filter generic matches
MIN_CHUNK_AGREEMENT = 2  # Minimum chunks required above threshold for multi-chunk agreement
AUTHORITY_BOOST_TEACHER = 0.04  # Optional metadata boost for teacher documents
AUTHORITY_BOOST_OFFICIAL = 0.05  # Optional metadata boost for official publications


class VectorDBService:
    """Service for managing ChromaDB vector database with persistent storage"""

    def __init__(self):
        # Initialize ChromaDB with PersistentClient as per specification
        self.client = chromadb.PersistentClient(
            path=settings.CHROMA_PERSIST_DIRECTORY,
            settings=ChromaSettings(
                anonymized_telemetry=False,
                allow_reset=True
            )
        )

        # Get or create collection
        self.collection = self.client.get_or_create_collection(
            name=settings.CHROMA_COLLECTION_NAME,
            metadata={"hnsw:space": "cosine"}
        )

        # Initialize embedding model (Local execution as per specification)
        logger.info(f"Loading embedding model: {settings.EMBEDDING_MODEL}")
        self.embedding_model = SentenceTransformer(settings.EMBEDDING_MODEL)

        logger.info(
            f"VectorDB initialized with collection '{settings.CHROMA_COLLECTION_NAME}' "
            f"at {settings.CHROMA_PERSIST_DIRECTORY}"
        )

    def generate_embedding(self, text: str) -> List[float]:
        """
        Generate embedding for text using local Sentence-Transformers model.

        Args:
            text: Text to embed

        Returns:
            List[float]: Embedding vector
        """
        embedding = self.embedding_model.encode(text, convert_to_numpy=True)
        return embedding.tolist()

    def generate_embeddings(self, texts: List[str]) -> List[List[float]]:
        """
        Generate embeddings for multiple texts in batch.

        Args:
            texts: List of texts to embed

        Returns:
            List[List[float]]: List of embedding vectors
        """
        embeddings = self.embedding_model.encode(texts, convert_to_numpy=True)
        return embeddings.tolist()

    async def index_document(
        self,
        file_id: int,
        chunks: List[str]
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
            # Generate embeddings for all chunks
            logger.info(f"Generating embeddings for {len(chunks)} chunks")
            embeddings = self.generate_embeddings(chunks)

            # Prepare IDs and metadata
            ids = [f"{file_id}_chunk_{i}" for i in range(len(chunks))]
            metadatas = [
                {
                    "file_id": file_id,
                    "chunk_index": i,
                    "text": chunk[:500]  # Store preview for debugging
                }
                for i, chunk in enumerate(chunks)
            ]

            # Upsert to ChromaDB
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

    async def index_text(
        self,
        text_id: str,
        chunks: List[str],
        metadata: Optional[Dict[str, Any]] = None
    ) -> int:
        """
        Index arbitrary text chunks with custom metadata (no MinIO required).

        Useful for indexing CV summaries, teacher profiles, or any text
        that doesn't come from a PDF document.

        Args:
            text_id: Unique identifier string (e.g. \"cv_summary_teacher_42\")
            chunks: List of text chunks
            metadata: Arbitrary metadata to store with vectors (e.g. teacher_id, teacher_name)

        Returns:
            int: Number of chunks indexed
        """
        try:
            logger.info(f"Generating embeddings for {len(chunks)} chunks (text_id={text_id})")
            embeddings = self.generate_embeddings(chunks)

            base_metadata = dict(metadata) if metadata else {}

            ids = [f"{text_id}_chunk_{i}" for i in range(len(chunks))]
            metadatas = [
                {
                    **base_metadata,
                    "chunk_index": i,
                    "text": chunk[:500]
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
                f"Successfully indexed {len(chunks)} chunks for text_id={text_id}"
            )

            return len(chunks)

        except Exception as e:
            logger.error(f"Error indexing text: {e}")
            raise

    async def search(
        self,
        query: str,
        top_k: int = 10
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
            # Generate query embedding
            query_embedding = self.generate_embedding(query)

            # Query ChromaDB - get more results to ensure we have enough after filtering
            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k * 5  # Increased multiplier to account for filtering
            )

            # Track all chunks per file_id for multi-chunk agreement and normalization
            file_chunks = {}  # {file_id: [scores]}
            file_metadata = {}  # {file_id: metadata} for authority boosts

            if results['metadatas'] and results['distances']:
                for metadata, distance in zip(
                    results['metadatas'][0],
                    results['distances'][0]
                ):
                    file_id = metadata['file_id']
                    # Convert distance to similarity score (1 - distance for cosine)
                    score = 1.0 - distance

                    # Apply minimum similarity threshold (mandatory)
                    if score < MIN_SIMILARITY:
                        continue

                    # Track all qualifying chunks for this file
                    if file_id not in file_chunks:
                        file_chunks[file_id] = []
                        file_metadata[file_id] = metadata

                    file_chunks[file_id].append(score)

            # Calculate normalized scores with ranking improvements
            normalized_matches = []

            for file_id, chunk_scores in file_chunks.items():
                # Multi-chunk agreement check (recommended)
                # A document should have at least 2 chunks above threshold
                if len(chunk_scores) < MIN_CHUNK_AGREEMENT:
                    logger.debug(
                        f"Skipping file_id={file_id}: only {len(chunk_scores)} chunk(s) above threshold"
                    )
                    continue

                # Get best chunk score
                best_chunk_score = max(chunk_scores)
                chunk_count = len(chunk_scores)

                # Apply chunk-count normalization (mandatory)
                # This reduces dominance of large documents
                normalized_score = best_chunk_score * (1.0 / math.log(chunk_count + 1))

                # Optional authority-aware metadata boost (production-ready)
                metadata = file_metadata[file_id]
                authority_boost = 0.0

                # Check for authority metadata if available
                if 'is_teacher_document' in metadata and metadata['is_teacher_document']:
                    authority_boost = AUTHORITY_BOOST_TEACHER
                elif 'is_official_publication' in metadata and metadata['is_official_publication']:
                    authority_boost = AUTHORITY_BOOST_OFFICIAL

                # Apply soft boost (never allows bad matches to rank high)
                final_score = min(1.0, normalized_score + authority_boost)

                normalized_matches.append({
                    "file_id": file_id,
                    "score": float(final_score),
                    "chunk_count": chunk_count,  # For debugging/monitoring
                    "best_chunk_score": float(best_chunk_score)  # For debugging/monitoring
                })

            # Sort by final score
            normalized_matches.sort(key=lambda x: x['score'], reverse=True)

            # Return top_k unique file_ids
            normalized_matches = normalized_matches[:top_k]

            # Clean up debugging fields before returning
            matches = [
                {"file_id": match["file_id"], "score": match["score"]}
                for match in normalized_matches
            ]

            logger.info(
                f"Search returned {len(matches)} unique file_ids for query: '{query[:50]}...' "
                f"(filtered by MIN_SIMILARITY={MIN_SIMILARITY}, MIN_CHUNK_AGREEMENT={MIN_CHUNK_AGREEMENT})"
            )

            return matches

        except Exception as e:
            logger.error(f"Error during search: {e}")
            raise

    async def search_summaries(
        self,
        query: str,
        top_k: int = 10
    ) -> List[Dict[str, Any]]:
        """
        Search only CV summary vectors and return teacher matches.

        Queries ChromaDB filtered by document_type=cv_summary,
        groups chunks by teacher_id, and returns teacher metadata + similarity.

        Args:
            query: Search query string
            top_k: Number of teacher results to return

        Returns:
            List[Dict]: List of {"teacher_id": int, "teacher_name": str, "score": float}
        """
        try:
            query_embedding = self.generate_embedding(query)

            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k * 5,
                where={"document_type": "cv_summary"}
            )

            teacher_chunks: Dict[int, List[float]] = {}
            teacher_meta: Dict[int, str] = {}

            if results['metadatas'] and results['distances']:
                for metadata, distance in zip(
                    results['metadatas'][0],
                    results['distances'][0]
                ):
                    score = 1.0 - distance
                    if score < 0.05:
                        continue

                    teacher_id = int(metadata.get("teacher_id", 0))
                    if teacher_id not in teacher_chunks:
                        teacher_chunks[teacher_id] = []
                        teacher_meta[teacher_id] = metadata.get("teacher_name", f"teacher_{teacher_id}")

                    teacher_chunks[teacher_id].append(score)

            matches = []
            for teacher_id, chunk_scores in teacher_chunks.items():
                best_score = max(chunk_scores)
                chunk_count = len(chunk_scores)
                if chunk_count > 1:
                    best_score *= (1.0 / math.log(chunk_count + 1))
                final_score = min(1.0, best_score)

                matches.append({
                    "teacher_id": teacher_id,
                    "teacher_name": teacher_meta[teacher_id],
                    "score": float(final_score),
                })

            matches.sort(key=lambda x: x['score'], reverse=True)
            matches = matches[:top_k]

            logger.info(
                f"Summary search returned {len(matches)} teachers for query: '{query[:50]}...'"
            )

            return matches

        except Exception as e:
            logger.error(f"Error during summary search: {e}")
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
            # Query to find all chunks for this file_id
            results = self.collection.get(
                where={"file_id": file_id}
            )

            if not results['ids']:
                logger.info(f"No vectors found for file_id={file_id}")
                return 0

            # Delete all matching IDs
            self.collection.delete(ids=results['ids'])

            deleted_count = len(results['ids'])
            logger.info(f"Deleted {deleted_count} chunks for file_id={file_id}")

            return deleted_count

        except Exception as e:
            logger.error(f"Error deleting vectors for file_id={file_id}: {e}")
            raise

    async def delete_by_metadata(self, metadata_filter: Dict[str, Any]) -> int:
        """
        Delete all vectors matching the given metadata filter.

        Args:
            metadata_filter: Dict of metadata key-value pairs to match (AND condition)

        Returns:
            int: Number of chunks deleted
        """
        try:
            results = self.collection.get(
                where=metadata_filter
            )

            if not results['ids']:
                logger.info(f"No vectors found for metadata filter: {metadata_filter}")
                return 0

            self.collection.delete(ids=results['ids'])

            deleted_count = len(results['ids'])
            logger.info(
                f"Deleted {deleted_count} chunks for metadata filter: {metadata_filter}"
            )

            return deleted_count

        except Exception as e:
            logger.error(f"Error deleting by metadata: {e}")
            raise

    def get_stats(self) -> Dict[str, Any]:
        """Get collection statistics"""
        try:
            count = self.collection.count()
            return {
                "collection_name": settings.CHROMA_COLLECTION_NAME,
                "total_vectors": count,
                "persist_directory": settings.CHROMA_PERSIST_DIRECTORY
            }
        except Exception as e:
            logger.error(f"Error getting stats: {e}")
            return {"error": str(e)}


# Singleton instance
vector_db_service = VectorDBService()
