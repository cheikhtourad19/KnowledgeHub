# AI Client Integration - Java Spring Boot DTOs

This document provides the complete AiClient interface and all related DTOs for integrating the AI and Search Service with the Spring Boot resource-service.

## 📍 Location
All files are in: `/Users/mac/Desktop/KnowledgeHub/resource-service/src/main/java/tn/esprim/knowledgehub/resource/`

---

## 🔌 AiClient Interface

**File**: `client/AiClient.java`

```java
package tn.esprim.knowledgehub.resource.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.esprim.knowledgehub.resource.client.dto.IndexRequest;
import tn.esprim.knowledgehub.resource.client.dto.SearchRequest;

@FeignClient(
        name = "ai-search-service",
        url = "http://localhost:8000"
)
public interface AiClient {

    /**
     * Index a document for semantic search
     * POST /api/v1/index
     */
    @PostMapping("/api/v1/index")
    IndexResponse indexDocument(@RequestBody IndexRequest request);

    /**
     * Perform semantic search
     * POST /api/v1/search
     */
    @PostMapping("/api/v1/search")
    SearchResponse semanticSearch(@RequestBody SearchRequest request);

    /**
     * Delete document from index
     * DELETE /api/v1/index/{fileId}
     */
    @DeleteMapping("/api/v1/index/{fileId}")
    DeleteResponse deleteDocument(@PathVariable("fileId") Integer fileId);
}
```

---

## 📥 Request DTOs

### 1. IndexRequest

**File**: `dto/request/IndexRequest.java`

```java
package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexRequest {

    @NotNull(message = "File ID is required")
    @Min(value = 1, message = "File ID must be greater than 0")
    private Integer fileId;

    @NotBlank(message = "Bucket name is required")
    private String bucket;

    @NotBlank(message = "Object name is required")
    private String objectName;
}
```

**Usage Example**:
```java
IndexRequest request = IndexRequest.builder()
    .fileId(123)
    .bucket("academic-documents")
    .objectName("pfe/2024/report_123.pdf")
    .build();

IndexResponse response = aiClient.indexDocument(request);
```

---

### 2. SearchRequest

**File**: `dto/request/SearchRequest.java`

```java
package tn.esprim.knowledgehub.resource.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {

    @NotBlank(message = "Query is required")
    private String query;

    @Min(value = 1, message = "Top K must be at least 1")
    @Max(value = 100, message = "Top K cannot exceed 100")
    private Integer topK = 10; // Default value
}
```

**Usage Example**:
```java
SearchRequest request = SearchRequest.builder()
    .query("machine learning algorithms for image classification")
    .topK(10)
    .build();

SearchResponse response = aiClient.semanticSearch(request);
```

---

## 📤 Response DTOs

### 1. IndexResponse

**File**: `dto/response/IndexResponse.java`

```java
package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IndexResponse {

    private Boolean success;
    
    private Integer fileId;
    
    private Integer chunksIndexed;
    
    private String message;
}
```

**Example Response**:
```json
{
  "success": true,
  "fileId": 123,
  "chunksIndexed": 42,
  "message": "Successfully indexed document with 42 chunks"
}
```

---

### 2. SearchResponse

**File**: `dto/response/SearchResponse.java`

```java
package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {

    private String query;
    
    private List<SearchMatch> matches;
    
    private Integer totalResults;
}
```

**Example Response**:
```json
{
  "query": "machine learning",
  "matches": [
    {"fileId": 123, "score": 0.89},
    {"fileId": 456, "score": 0.75}
  ],
  "totalResults": 2
}
```

---

### 3. SearchMatch

**File**: `dto/response/SearchMatch.java`

```java
package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMatch {

    private Integer fileId;
    
    private Double score;
}
```

---

### 4. DeleteResponse

**File**: `dto/response/DeleteResponse.java`

```java
package tn.esprim.knowledgehub.resource.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeleteResponse {

    private Boolean success;
    
    private Integer fileId;
    
    private Integer chunksDeleted;
    
    private String message;
}
```

**Example Response**:
```json
{
  "success": true,
  "fileId": 123,
  "chunksDeleted": 42,
  "message": "Successfully deleted 42 chunks"
}
```

---

## 🔧 Integration Example

### Service Layer Usage

```java
@Service
@RequiredArgsConstructor
public class ResourceService {
    
    private final AiClient aiClient;
    
    public void indexDocument(Integer fileId, String bucket, String objectName) {
        // Create index request
        IndexRequest request = IndexRequest.builder()
            .fileId(fileId)
            .bucket(bucket)
            .objectName(objectName)
            .build();
        
        // Call AI service
        IndexResponse response = aiClient.indexDocument(request);
        
        log.info("Indexed document {} with {} chunks", 
            response.getFileId(), 
            response.getChunksIndexed());
    }
    
    public List<Integer> searchDocuments(String query, int topK) {
        // Create search request
        SearchRequest request = SearchRequest.builder()
            .query(query)
            .topK(topK)
            .build();
        
        // Call AI service
        SearchResponse response = aiClient.semanticSearch(request);
        
        // Extract file IDs
        return response.getMatches().stream()
            .map(SearchMatch::getFileId)
            .collect(Collectors.toList());
    }
    
    public void deleteDocumentIndex(Integer fileId) {
        // Call AI service
        DeleteResponse response = aiClient.deleteDocument(fileId);
        
        log.info("Deleted {} chunks for fileId {}", 
            response.getChunksDeleted(), 
            response.getFileId());
    }
}
```

---

## 📋 API Endpoint Mapping

| Java Method | HTTP Method | Endpoint | Purpose |
|------------|-------------|----------|---------|
| `indexDocument()` | POST | `/api/v1/index` | Index a PDF document |
| `semanticSearch()` | POST | `/api/v1/search` | Search indexed documents |
| `deleteDocument()` | DELETE | `/api/v1/index/{fileId}` | Remove document from index |

---

## ✅ Validation Rules

### IndexRequest
- ✅ `fileId`: Required, must be > 0
- ✅ `bucket`: Required, not blank
- ✅ `objectName`: Required, not blank

### SearchRequest
- ✅ `query`: Required, not blank
- ✅ `topK`: Optional (default: 10), range: 1-100

---

## 🎯 Key Features

1. **Type Safety**: All DTOs use proper Java types (Integer, String, Boolean, Double)
2. **Validation**: JSR-303 validation annotations on request DTOs
3. **Lombok**: Reduces boilerplate with @Data, @Builder, etc.
4. **Feign Integration**: Seamless HTTP client with Spring Cloud OpenFeign
5. **Documentation**: Clear JavaDocs and usage examples

---

## 📝 Notes

- All DTOs match the Python FastAPI models exactly
- The `fileId` is used to maintain sync with PostgreSQL database
- Scores range from 0.0 to 1.0 (higher = more similar)
- The AI service runs on `http://localhost:8000`
- All endpoints use `/api/v1` prefix

---

**Status**: ✅ All DTOs created and validated  
**Integration**: ✅ Ready for use in resource-service
