"""
AI and Search Service - Semantic Search Microservice
FastAPI application for semantic search over academic PDFs
"""
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.config import settings
from app.routers.search import router as search_router
from app.routers.ask import router as ask_router
from app.routers.search_v2 import router as search_router_v2
from app.services.mongo_service import mongo_text_cache
import logging

# Configure logging
logging.basicConfig(
    level=logging.INFO if not settings.DEBUG else logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)

logger = logging.getLogger(__name__)

# Create FastAPI application
app = FastAPI(
    title=settings.SERVICE_NAME,
    description="Semantic Search Microservice for Knowledge Hub - Provides AI-powered search over academic PDFs",
    version=settings.VERSION,
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Configure appropriately for production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Include routers
app.include_router(search_router, prefix="/api/v1")
app.include_router(ask_router, prefix="/api/v1")
app.include_router(search_router_v2, prefix="/api/v2")

@app.on_event("startup")
async def startup_event():
    """Initialize services on startup"""
    logger.info(f"Starting {settings.SERVICE_NAME} v{settings.VERSION}")
    logger.info(f"ChromaDB persist directory: {settings.CHROMA_PERSIST_DIRECTORY}")
    logger.info(f"Embedding model: {settings.EMBEDDING_MODEL}")
    logger.info(f"Chunk size: {settings.CHUNK_SIZE}, Overlap: {settings.CHUNK_OVERLAP}")
    logger.info(f"MinIO endpoint: {settings.MINIO_ENDPOINT}")
    # Connect to MongoDB text cache
    await mongo_text_cache.connect()
    logger.info(f"MongoDB URI: {settings.MONGO_URI}")

@app.on_event("shutdown")
async def shutdown_event():
    """Cleanup on shutdown"""
    logger.info(f"Shutting down {settings.SERVICE_NAME}")
    await mongo_text_cache.disconnect()

@app.get("/")
async def root():
    """Root endpoint"""
    return {
        "service": settings.SERVICE_NAME,
        "version": settings.VERSION,
        "status": "running",
        "docs": "/docs"
    }


if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "main:app",
        host=settings.API_HOST,
        port=settings.API_PORT,
        reload=settings.DEBUG
    )


