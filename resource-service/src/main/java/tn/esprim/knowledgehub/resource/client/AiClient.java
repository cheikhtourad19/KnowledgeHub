package tn.esprim.knowledgehub.resource.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.esprim.knowledgehub.resource.client.dto.*;
import tn.esprim.knowledgehub.resource.config.FeignConfig;

@FeignClient(
        name = "ai-search-service",
        url = "${ai-search.service.url:http://localhost:8000}",  // Fixed syntax
        configuration = FeignConfig.class
)
public interface AiClient {

    @PostMapping("/api/v2/index")
    IndexResponse indexDocument(@RequestBody IndexRequest request);

    @PostMapping("/api/v2/search")
    SearchResponse semanticSearch(@RequestBody SearchRequest request);

    @DeleteMapping("/api/v2/index/{fileId}")
    DeleteResponse deleteDocument(@PathVariable("fileId") Integer fileId);

    @PostMapping("/api/v2/ask")
    AskResponse askDocumentQuestion(@RequestBody AskRequest request);
}