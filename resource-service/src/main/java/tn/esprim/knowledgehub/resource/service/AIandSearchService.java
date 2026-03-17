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

    public File indexFile(Long fileId) {
        File file = fileRepository.findById(fileId).orElseThrow();

        try {
            aiClient.indexDocument(
                    IndexRequest.builder()
                            .bucket(file.getStoragePath().split("/")[0])
                            .file_id(Math.toIntExact(fileId))
                            .object_name(file.getStoragePath().split("/")[1])
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;

    }

    public void deleteFile(Integer fileId) {
        try {
            DeleteResponse response = aiClient.deleteDocument(fileId);
            log.info("File with id {} deleted from vector DB: {} chunks deleted", fileId, response.getChunksDeleted());
        } catch (Exception e) {
            log.error("Error deleting file with id {} from vector DB: {}", fileId, e.getMessage());
            throw new RuntimeException("Failed to delete file from vector DB", e);
        }
    }

    public List<File> searchFile(String query) {

        SearchRequest request = SearchRequest.builder()
                .query(query)
                .build();
        SearchResponse response = aiClient.semanticSearch(request);
        if (response.getTotalResults() == 0) {
            throw new RuntimeException("No result found");
        }
        List<Long> extracted_ids = new ArrayList<>();
        for (SearchMatch searchMatch : response.getMatches()) {
            extracted_ids.add(Long.valueOf(searchMatch.getFileId()));
        }
        List<File> files = new ArrayList<>();
        for (Long id : extracted_ids) {
            try {

                File file_extracted = fileRepository.findById(id).orElseThrow();
                if (file_extracted.getIsPublished()) {
                    files.add(file_extracted);
                }
            } catch (Exception e) {
                log.error("Error while searching for file with id: {}", id);
            }

        }
        return files;
    }

    public AskResponse askQuestionAboutDocument(Long fileId, String question) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found with ID: " + fileId));

        StorageInfo storageInfo = extractStorageInfo(file.getStoragePath());

        AskRequest askRequest = AskRequest.builder()
                .file_id(Math.toIntExact(fileId))
                .bucket(storageInfo.bucket())
                .object_name(storageInfo.objectName())
                .query(question)
                .build();

        return aiClient.askDocumentQuestion(askRequest);
    }

    private StorageInfo extractStorageInfo(String storagePath) {
        if (storagePath == null || !storagePath.contains("/")) {
            throw new IllegalArgumentException("Invalid storage path: " + storagePath);
        }
        String[] pathParts = storagePath.split("/", 2);
        if (pathParts.length < 2 || pathParts[0].isBlank() || pathParts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid storage path: " + storagePath);
        }
        return new StorageInfo(pathParts[0], pathParts[1]);
    }

    private record StorageInfo(String bucket, String objectName) {
    }
}
