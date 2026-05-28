package tn.esprim.knowledgehub.user.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.user.client.dto.ExtractTextResponse;
import tn.esprim.knowledgehub.user.client.dto.IndexSummaryRequest;
import tn.esprim.knowledgehub.user.client.dto.IndexSummaryResponse;
import tn.esprim.knowledgehub.user.client.dto.SummarizeRequest;
import tn.esprim.knowledgehub.user.client.dto.SummarizeResponse;
import tn.esprim.knowledgehub.user.config.FeignConfig;

import java.util.Map;

@FeignClient(
        name = "ai-search-service",
        url = "${ai-search.service.url:http://localhost:8000}",
        configuration = FeignConfig.class
)
public interface AiServiceClient {

    @PostMapping(value = "/api/v1/extract-pdf-text", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ExtractTextResponse extractPdfText(@RequestPart("file") MultipartFile file);

    @PostMapping("/api/v1/summarize-text")
    SummarizeResponse summarizeText(@RequestBody SummarizeRequest request);

    @PostMapping("/api/v1/index-summary")
    IndexSummaryResponse indexSummary(@RequestBody IndexSummaryRequest request);

    @DeleteMapping("/api/v1/index-summary/{teacherId}")
    Map<String, Object> deleteSummaryByTeacher(@PathVariable("teacherId") Long teacherId);
}
