"""MongoDB service for caching extracted PDF text by file_id"""
import logging
from motor.motor_asyncio import AsyncIOMotorClient
from app.config import settings

logger = logging.getLogger(__name__)


class MongoTextCacheService:
    """
    Async MongoDB service that stores and retrieves extracted PDF text.

    Collection schema:
        {
            "_id":           <file_id: int>,
            "extracted_text": <str>,
            "created_at":    <datetime>
        }
    """

    def __init__(self):
        self.client: AsyncIOMotorClient | None = None
        self.collection = None

    async def connect(self):
        """Initialize the MongoDB connection (called at app startup)."""
        self.client = AsyncIOMotorClient(settings.MONGO_URI)
        db = self.client[settings.MONGO_DB_NAME]
        self.collection = db[settings.MONGO_COLLECTION_NAME]
        # Ensure index on _id (file_id) – MongoDB does this by default, but be explicit
        logger.info(
            f"MongoDB connected: {settings.MONGO_URI} | "
            f"db={settings.MONGO_DB_NAME} | col={settings.MONGO_COLLECTION_NAME}"
        )

    async def disconnect(self):
        """Close the MongoDB connection (called at app shutdown)."""
        if self.client:
            self.client.close()
            logger.info("MongoDB connection closed")

    async def get_text(self, file_id: int) -> str | None:
        """
        Retrieve cached extracted text for a given file_id.

        Returns:
            str if found, None otherwise.
        """
        doc = await self.collection.find_one({"_id": file_id}, {"extracted_text": 1})
        if doc:
            logger.info(f"Cache HIT for file_id={file_id}")
            return doc["extracted_text"]
        logger.info(f"Cache MISS for file_id={file_id}")
        return None

    async def save_text(self, file_id: int, extracted_text: str) -> None:
        """
        Upsert extracted text for a file_id.
        If the document already exists it is replaced with fresh content.
        """
        from datetime import datetime, timezone
        await self.collection.update_one(
            {"_id": file_id},
            {
                "$set": {
                    "extracted_text": extracted_text,
                    "updated_at": datetime.now(timezone.utc),
                },
                "$setOnInsert": {
                    "created_at": datetime.now(timezone.utc),
                },
            },
            upsert=True,
        )
        logger.info(
            f"Saved extracted text for file_id={file_id} "
            f"({len(extracted_text)} chars)"
        )

    def is_connected(self) -> bool:
        return self.client is not None


# Singleton — connected during startup lifecycle
mongo_text_cache = MongoTextCacheService()
