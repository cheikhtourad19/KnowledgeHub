package tn.esprim.knowledgehub.resource.controller.library;

import feign.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.service.AIandSearchService;
import tn.esprim.knowledgehub.resource.service.FileService;

@RestController
@RequestMapping("/api/public/library")
@RequiredArgsConstructor
public class LibraryController {

    private final AIandSearchService aiandSearchService;
    private final FileService fileService;

    @GetMapping("/getDocuments")
    public ResponseEntity<ApiResponse> getDocuments(){

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Documents retrieved successfully")
                            .data(fileService.getAllFiles())
                            .build()
            );
    }



}
