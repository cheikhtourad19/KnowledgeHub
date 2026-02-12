# AI and Search Service - Setup Complete ✅

## Overview
The **AI and Search Service** is now fully initialized with FastAPI and configured for semantic search over academic PDFs. The service integrates with MinIO for PDF storage and ChromaDB for vector embeddings.

## ✅ What's Been Done

### 1. FastAPI Application Structure
- ✅ Main FastAPI application (`main.py`)
- ✅ Configuration management (`app/config.py`)
- ✅ Pydantic models for requests/responses (`app/models.py`)
- ✅ API routers (`app/routers/search.py`)
- ✅ Service layer implementations:
  - MinIO Service (PDF fetching)
  - PDF Extraction Service (PyMuPDF/fitz)
  - Text Chunking Service (LangChain)
  - Vector DB Service (ChromaDB with persistent storage)

### 2. Dependencies Installed
All dependencies are defined in `requirements.txt`:
- FastAPI & Uvicorn (web framework)
- PyMuPDF (PDF text extraction)
- MinIO Python SDK
- ChromaDB (vector database)
- Sentence-Transformers (local embeddings)
- LangChain (text splitting)

### 3. API Endpoints Implemented

#### POST `/api/v1/index`
Index a document from MinIO for semantic search.
- **Request**: `{ "file_id": int, "bucket": str, "object_name": str }`
- **Response**: `{ "success": bool, "file_id": int, "chunks_indexed": int, "message": str }`

#### POST `/api/v1/search`
Perform semantic search over indexed documents.
- **Request**: `{ "query": str, "top_k": int }`
- **Response**: `{ "query": str, "matches": [{"file_id": int, "score": float}], "total_results": int }`

#### DELETE `/api/v1/index/{fileId}`
Delete all vectors associated with a file.
- **Response**: `{ "success": bool, "file_id": int, "chunks_deleted": int, "message": str }`

#### GET `/api/v1/health`
Health check endpoint.
- **Response**: Service status, vector DB stats, MinIO connection status

### 4. Java Spring Boot Integration

#### AiClient Interface
Created Feign client for Spring Boot integration:
```java
@FeignClient(name = "ai-search-service", url = "http://localhost:8000")
public interface AiClient {
    @PostMapping("/api/v1/index")
    IndexResponse indexDocument(@RequestBody IndexRequest request);
    
    @PostMapping("/api/v1/search")
    SearchResponse semanticSearch(@RequestBody SearchRequest request);
    
    @DeleteMapping("/api/v1/index/{fileId}")
    DeleteResponse deleteDocument(@PathVariable("fileId") Integer fileId);
}
```

#### DTOs Created
All request/response DTOs have been created in the resource-service:
- ✅ `IndexRequest.java`
- ✅ `IndexResponse.java`
- ✅ `SearchRequest.java`
- ✅ `SearchResponse.java`
- ✅ `SearchMatch.java`
- ✅ `DeleteResponse.java`

## 🚀 How to Run

### Prerequisites
- Python 3.8+
- pip/pip3
- MinIO server running on `localhost:9000`

### Installation
```bash
cd /Users/mac/Desktop/KnowledgeHub/ai-and-search-service

# Install dependencies
pip3 install -r requirements.txt
```

### Start the Service
```bash
# Using uvicorn directly
uvicorn main:app --reload --host 0.0.0.0 --port 8000

# Or using Python
python3 -m uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

### Access Documentation
- **Swagger UI**: http://localhost:8000/docs
- **ReDoc**: http://localhost:8000/redoc

## 🔧 Configuration

Configuration is managed through `.env` file or environment variables:

```bash
# Service Configuration
SERVICE_NAME="AI and Search Service"
VERSION="1.0.0"
DEBUG=false

# API Configuration
API_HOST="0.0.0.0"
API_PORT=8000

# MinIO Configuration
MINIO_ENDPOINT="localhost:9000"
MINIO_ACCESS_KEY="minioadmin"
MINIO_SECRET_KEY="minioadmin"
MINIO_SECURE=false

# Vector Database
CHROMA_PERSIST_DIRECTORY="./chroma_db"
CHROMA_COLLECTION_NAME="documents"

# Embedding Model (Local - No external APIs)
EMBEDDING_MODEL="sentence-transformers/all-MiniLM-L6-v2"

# Text Processing
CHUNK_SIZE=1000
CHUNK_OVERLAP=100
```

## 🔒 Security & Privacy Features

✅ **No Local File Storage**: PDFs processed as byte streams in memory  
✅ **Local Embeddings**: Uses Sentence-Transformers (no OpenAI/Gemini)  
✅ **Persistent Vector DB**: ChromaDB data survives restarts  
✅ **File ID Mapping**: Each vector tagged with `file_id` for PostgreSQL sync  

## 📦 Project Structure

```
ai-and-search-service/
├── main.py                      # FastAPI application entry point
├── requirements.txt             # Python dependencies
├── .env                         # Environment configuration
├── collection.json              # Postman collection for testing
├── app/
│   ├── __init__.py
│   ├── config.py               # Settings & configuration
│   ├── models.py               # Pydantic request/response models
│   ├── routers/
│   │   ├── __init__.py
│   │   └── search.py           # API endpoints
│   └── services/
│       ├── __init__.py
│       ├── minio_service.py    # MinIO integration
│       ├── pdf_service.py      # PDF text extraction
│       ├── text_chunking_service.py  # Text splitting
│       └── vector_db_service.py      # ChromaDB operations
└── tests/                       # (Empty - ready for tests)
```

## 🧪 Testing

Use the provided Postman collection (`collection.json`) or test via Swagger UI.

### Example: Index a Document
```bash
curl -X POST "http://localhost:8000/api/v1/index" \
  -H "Content-Type: application/json" \
  -d '{
    "file_id": 123,
    "bucket": "academic-documents",
    "object_name": "pfe/2024/report_123.pdf"
  }'
```

### Example: Search
```bash
curl -X POST "http://localhost:8000/api/v1/search" \
  -H "Content-Type: application/json" \
  -d '{
    "query": "machine learning algorithms",
    "top_k": 10
  }'
```

## ✅ Code Quality

- Fixed incorrect `str` import in `pdf_service.py`
- All configuration follows specification requirements
- Proper error handling and logging
- Type hints throughout
- No errors in Java DTOs

## 📝 Next Steps

1. **Install Dependencies**: Run `pip3 install -r requirements.txt`
2. **Start MinIO**: Ensure MinIO is running on port 9000
3. **Start Service**: Run `uvicorn main:app --reload --port 8000`
4. **Test Integration**: Use Postman collection or Swagger UI
5. **Connect Spring Boot**: Use the AiClient with resource-service

## 🎯 Specification Compliance

✅ Framework: FastAPI  
✅ Vector DB: ChromaDB (Persistent)  
✅ Embeddings: all-MiniLM-L6-v2 (Local)  
✅ PDF Extraction: PyMuPDF (fitz)  
✅ Storage: MinIO Python SDK  
✅ No local file storage (byte streams only)  
✅ Privacy: Local execution only  
✅ Chunk size: 1000, overlap: 100  
✅ File ID metadata mapping  

---

**Service Status**: ✅ Ready for Development & Testing
