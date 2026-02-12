"""Vector database service using ChromaDB with persistent storage"""
from typing import List, Dict, Any
import chromadb
from chromadb.config import Settings as ChromaSettings
from sentence_transformers import SentenceTransformer
from app.config import settings
import logging

logger = logging.getLogger(__name__)


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

    async def search(
        self,
        query: str,
        top_k: int = 10
    ) -> List[Dict[str, Any]]:
        """
        Perform semantic search and return unique file_ids with scores.

        Args:
            query: Search query string
            top_k: Number of results to return

        Returns:
            List[Dict]: List of {"file_id": int, "score": float}
        """
        try:
            # Generate query embedding
            query_embedding = self.generate_embedding(query)

            # Query ChromaDB
            results = self.collection.query(
                query_embeddings=[query_embedding],
                n_results=top_k * 3  # Get more results to filter unique file_ids
            )

            # Extract unique file_ids with their best scores
            file_scores = {}

            if results['metadatas'] and results['distances']:
                for metadata, distance in zip(
                    results['metadatas'][0],
                    results['distances'][0]
                ):
                    file_id = metadata['file_id']
                    # Convert distance to similarity score (1 - distance for cosine)
                    score = 1.0 - distance

                    # Keep the best score for each file_id
                    if file_id not in file_scores or score > file_scores[file_id]:
                        file_scores[file_id] = score

            # Format results and sort by score
            matches = [
                {"file_id": file_id, "score": float(score)}
                for file_id, score in file_scores.items()
            ]
            matches.sort(key=lambda x: x['score'], reverse=True)

            # Return top_k unique file_ids
            matches = matches[:top_k]

            logger.info(
                f"Search returned {len(matches)} unique file_ids for query: '{query[:50]}...'"
            )

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
