package tn.esprim.knowledgehub.resource.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.esprim.knowledgehub.resource.client.AiClient;
import tn.esprim.knowledgehub.resource.client.dto.*;
import tn.esprim.knowledgehub.resource.entity.File;
import tn.esprim.knowledgehub.resource.repository.FileRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIandSearchService {
    private final FileRepository fileRepository;
    private final AiClient aiClient;
    public File indexFile(Long fileId){
        File file = fileRepository.findById(fileId).orElseThrow();

        try {
            IndexResponse response = aiClient.indexDocument(
                    IndexRequest.builder()
                            .bucket(file.getStoragePath().split("/")[0])
                            .fileId(Math.toIntExact(fileId))
                            .objectName(file.getStoragePath().split("/")[1])
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;

    }
    public List<File> searchFile(String query){

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .build();
        SearchResponse response = aiClient.semanticSearch(request);
        if (response.getTotalResults()==0){
            throw new RuntimeException("No result found");
        }
        List<Long> extracted_ids= new ArrayList<>();
        for (SearchMatch searchMatch : response.getMatches()) {
            extracted_ids.add(Long.valueOf(searchMatch.getFileId()));
        }
        return fileRepository.findAllById(extracted_ids);
    }

}
