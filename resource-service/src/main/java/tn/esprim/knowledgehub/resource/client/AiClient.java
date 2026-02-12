package tn.esprim.knowledgehub.resource.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.esprim.knowledgehub.resource.client.dto.*;


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
