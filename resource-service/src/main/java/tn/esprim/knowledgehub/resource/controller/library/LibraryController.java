package tn.esprim.knowledgehub.resource.controller.library;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprim.knowledgehub.resource.client.dto.ApiResponse;
import tn.esprim.knowledgehub.resource.client.dto.SearchRequest;
import tn.esprim.knowledgehub.resource.enums.FileType;
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
        return ResponseEntity.ok(
                ApiResponse.builder()
                        .success(true)
                        .data(aiandSearchService.searchFile(request.getQuery()))
                        .message("result : "+aiandSearchService.searchFile(request.getQuery()).size())
                        .build()
        );
    }






}
