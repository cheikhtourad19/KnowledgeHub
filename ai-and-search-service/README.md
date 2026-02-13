# AI and Search Service - Semantic Search Microservice

## Overview

This is the **AI Brain** for Knowledge Hub - a FastAPI-based semantic search microservice that provides intelligent search over confidential academic PDFs (PFA, PFE, Internship Reports). It uses local AI models for privacy and security.

## Core Features

- **Semantic Search**: AI-powered search using sentence embeddings
- **Privacy-First**: All processing happens locally - no external APIs
- **Zero Local Storage**: PDFs processed as byte streams from MinIO
- **Persistent Vectors**: ChromaDB with persistent storage
- **Unique Mapping**: Every vector tagged with `file_id` for PostgreSQL sync

## Technical Stack

- **Framework**: FastAPI
- **Vector Database**: ChromaDB (PersistentClient)
- **Embeddings**: `all-MiniLM-L6-v2` via Sentence-Transformers (Local)
- **PDF Extraction**: PyMuPDF (fitz)
- **Text Splitting**: LangChain RecursiveCharacterTextSplitter
- **Storage Access**: MinIO Python SDK
- **Async Processing**: Native async/await support

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    FastAPI Application                      │
├─────────────────────────────────────────────────────────────┤
│  POST /api/v1/index     │ Index PDF from MinIO              │
│  POST /api/v1/search    │ Semantic search                   │
│  DELETE /api/v1/index/{id} │ Delete document vectors       │
│  GET /api/v1/health     │ Health check                      │
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        │                     │                     │
   ┌────▼────┐          ┌─────▼──────┐      ┌─────▼─────┐
   │ MinIO   │          │  ChromaDB  │      │ Sentence  │
   │ Storage │          │  (Persist) │      │Transformers│
   └─────────┘          └────────────┘      └───────────┘
```

## API Endpoints

### 1. Index Document
```http
POST /api/v1/index
Content-Type: application/json

{
  "file_id": 123,
  "bucket": "academic-documents",
  "object_name": "pfe/2024/report_123.pdf"
}
```

**Response:**
```json
{
  "success": true,
  "file_id": 123,
  "chunks_indexed": 15,
  "message": "Successfully indexed document with 15 chunks"
}
```

**Process:**
1. Fetch PDF from MinIO as byte stream (no local storage)
2. Extract text using PyMuPDF
3. Split text: chunk_size=1000, overlap=100
4. Generate embeddings locally
5. Upsert to ChromaDB with file_id in metadata

### 2. Semantic Search
```http
POST /api/v1/search
Content-Type: application/json

{
  "query": "machine learning algorithms",
  "top_k": 10
}
```

**Response:**
```json
{
  "query": "machine learning algorithms",
  "matches": [
    {"file_id": 123, "score": 0.89},
    {"file_id": 456, "score": 0.75}
  ],
  "total_results": 2
}
```

**Process:**
1. Generate embedding for query
2. Query ChromaDB for similar chunks
3. Extract UNIQUE file_ids
4. Return with similarity scores

### 3. Delete Document
```http
DELETE /api/v1/index/123
```

**Response:**
```json
{
  "success": true,
  "file_id": 123,
  "chunks_deleted": 15,
  "message": "Successfully deleted 15 chunks"
}
```

### 4. Health Check
```http
GET /api/v1/health
```

**Response:**
```json
{
  "status": "healthy",
  "service": "AI and Search Service",
  "version": "1.0.0",
  "vector_db_stats": {
    "collection_name": "documents",
    "total_vectors": 1250
  },
  "minio_connected": true
}
```

## Installation & Setup

### Prerequisites
- Python 3.9+
- MinIO server running
- At least 2GB RAM for embedding model

### 1. Install Dependencies
```bash
cd ai-and-search-service
pip install -r requirements.txt
```

### 2. Configure Environment
```bash
cp .env.example .env
# Edit .env with your MinIO credentials
```

**Key Configuration:**
```env
# MinIO
MINIO_ENDPOINT=localhost:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Vector DB
CHROMA_PERSIST_DIRECTORY=./chroma_db
CHROMA_COLLECTION_NAME=documents

# Embedding Model (Local)
EMBEDDING_MODEL=sentence-transformers/all-MiniLM-L6-v2

# Text Processing
CHUNK_SIZE=1000
CHUNK_OVERLAP=100
```

### 3. Run the Service
```bash
# Development
python main.py

# Production
uvicorn main:app --host 0.0.0.0 --port 8000
```

The service will be available at `http://localhost:8000`
- API docs: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

## Data Flow

### Indexing Flow
```
Spring Boot → POST /api/v1/index → MinIO (fetch PDF)
    → PyMuPDF (extract text) → LangChain (chunk)
    → SentenceTransformers (embed) → ChromaDB (store with file_id)
```

### Search Flow
```
User Query → POST /api/v1/search → SentenceTransformers (embed query)
    → ChromaDB (similarity search) → Extract unique file_ids
    → Return [{"file_id": X, "score": Y}] → Spring Boot
```

## Security & Privacy

✅ **No External APIs**: All AI processing happens locally
✅ **No Local File Storage**: PDFs processed as byte streams in RAM
✅ **Unique Mapping**: Every vector has `file_id` metadata for PostgreSQL sync
✅ **Persistent Storage**: ChromaDB data survives service restarts
✅ **Secure MinIO**: Credentials via environment variables

## Project Structure

```
ai-and-search-service/
├── main.py                     # FastAPI application entry point
├── requirements.txt            # Python dependencies
├── .env.example               # Environment template
├── README.md                  # This file
├── app/
│   ├── __init__.py
│   ├── config.py             # Configuration settings
│   ├── models.py             # Pydantic models
│   ├── routers/
│   │   └── search.py         # API endpoints
│   └── services/
│       ├── minio_service.py      # MinIO client
│       ├── pdf_service.py        # PyMuPDF extraction
│       ├── text_chunking_service.py  # Text splitting
│       └── vector_db_service.py   # ChromaDB + embeddings
└── chroma_db/                # Persistent vector storage
```

## Development

### Testing Endpoints

```bash
# Index a document
curl -X POST http://localhost:8000/api/v1/index \
  -H "Content-Type: application/json" \
  -d '{
    "file_id": 123,
    "bucket": "test-bucket",
    "object_name": "test.pdf"
  }'

# Search
curl -X POST http://localhost:8000/api/v1/search \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning",
    "top_k": 5
  }'

# Delete
curl -X DELETE http://localhost:8000/api/v1/index/123

# Health check
curl http://localhost:8000/api/v1/health
```

### Logs
Service logs include detailed information about:
- PDF fetching and extraction
- Text chunking statistics
- Embedding generation
- Vector operations

## Integration with Spring Boot

The Spring Boot backend calls this service for:
1. **Indexing**: When a PDF is uploaded to MinIO
2. **Search**: When users perform semantic search
3. **Deletion**: When documents are removed

Example Spring Boot integration:
```java
// Index document
RestTemplate restTemplate = new RestTemplate();
IndexRequest request = new IndexRequest(fileId, bucket, objectName);
restTemplate.postForObject(
    "http://ai-service:8000/api/v1/index", 
    request, 
    IndexResponse.class
);
```

## Performance

- **Embedding Model**: ~384 dimensions, fast inference
- **Indexing Speed**: ~1-2 seconds per page
- **Search Speed**: <100ms for most queries
- **Memory**: ~1GB for model + vectors
- **Chunk Size**: 1000 characters with 100 overlap

## Troubleshooting

### Issue: MinIO connection failed
- Check `MINIO_ENDPOINT` in `.env`
- Verify MinIO is running
- Check network connectivity

### Issue: ChromaDB errors
- Delete `chroma_db/` folder and restart
- Check disk space
- Verify write permissions

### Issue: Embedding model download
- First run downloads ~90MB model
- Ensure internet connection
- Model cached in `~/.cache/huggingface/`

## License

This service is part of the Knowledge Hub project.
