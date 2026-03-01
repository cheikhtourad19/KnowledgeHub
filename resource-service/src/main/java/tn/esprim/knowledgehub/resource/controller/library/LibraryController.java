package tn.esprim.knowledgehub.resource.controller.library;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.client.dto.SearchRequest;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.enums.FileType;
import tn.esprim.knowledgehub.resource.service.AIandSearchService;
import tn.esprim.knowledgehub.resource.service.FileService;

import java.util.List;

@RestController
@RequestMapping("/api/public/library")
@RequiredArgsConstructor
@Slf4j
public class LibraryController {

    private final AIandSearchService aiandSearchService;
    private final FileService fileService;

    @GetMapping("/getDocuments")
    public ResponseEntity<ApiResponse> getDocuments(){

            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .message("Documents retrieved successfully")
                            .data(fileService.getPublishedFiles())
                            .build()
            );
    }
    @GetMapping("/getDocuments/{type}")
    public ResponseEntity<ApiResponse> getDocumentsByType(@PathVariable FileType type){
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .message("Documents retrieved successfully")
                        .data(fileService.getFilesByType(type))
                        .build()
        );

    }

    @PostMapping("/ai-search")
    public ResponseEntity<ApiResponse> searchdocument(
            @RequestBody SearchRequest request
    ){
        try {
            List<File> files = aiandSearchService.searchFile(request.getQuery());
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(true)
                            .data(files)
                            .message("result : " + files.size())
                            .build()
            );
        }catch (Exception e){
            log.error("Error during AI search: {}", e.getMessage(), e);
            return ResponseEntity.ok(
                    ApiResponse.builder()
                            .success(false)
                            .data(null)
                            .message("No results found: " + e.getMessage())
                            .build());
        }
    }






}
