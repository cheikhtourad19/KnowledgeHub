"""MinIO service for fetching PDF files as byte streams"""
from io import BytesIO
from minio import Minio
from minio.error import S3Error
from app.config import settings
import logging

logger = logging.getLogger(__name__)


class MinIOService:
    """Service for interacting with MinIO storage"""

    def __init__(self):
        self.client = Minio(
            settings.MINIO_ENDPOINT,
            access_key=settings.MINIO_ACCESS_KEY,
            secret_key=settings.MINIO_SECRET_KEY,
            secure=settings.MINIO_SECURE
        )
        logger.info(f"MinIO client initialized for {settings.MINIO_ENDPOINT}")

    async def get_pdf_as_bytes(self, bucket: str, object_name: str) -> BytesIO:
        """
        Fetch PDF from MinIO as byte stream (NO local file storage).

        Args:
            bucket: MinIO bucket name
            object_name: Object name/path in the bucket

        Returns:
            BytesIO: PDF content as byte stream in memory

        Raises:
            S3Error: If object doesn't exist or other MinIO errors
            ValueError: If file is too large
        """
        try:
            # Get object stats to check size
            stats = self.client.stat_object(bucket, object_name)
            file_size_mb = stats.size / (1024 * 1024)

            if file_size_mb > settings.MAX_FILE_SIZE_MB:
                raise ValueError(
                    f"File size ({file_size_mb:.2f} MB) exceeds "
                    f"maximum allowed size ({settings.MAX_FILE_SIZE_MB} MB)"
                )

            # Fetch object directly into memory
            response = self.client.get_object(bucket, object_name)
            pdf_bytes = BytesIO(response.read())
            response.close()
            response.release_conn()

            logger.info(
                f"Successfully fetched {object_name} from {bucket} "
                f"({file_size_mb:.2f} MB)"
            )

            return pdf_bytes

        except S3Error as e:
            logger.error(f"MinIO error fetching {bucket}/{object_name}: {e}")
            raise
        except Exception as e:
            logger.error(f"Error fetching PDF from MinIO: {e}")
            raise

    def check_connection(self) -> bool:
        """Check if MinIO connection is working"""
        try:
            self.client.list_buckets()
            return True
        except Exception as e:
            logger.error(f"MinIO connection check failed: {e}")
            return False


# Singleton instance
minio_service = MinIOService()
