package tn.esprim.knowledgehub.resource.service;

import io.minio.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tn.esprim.knowledgehub.resource.entity.AnnonceStage;
import tn.esprim.knowledgehub.resource.entity.AnnonceUrl;
import tn.esprim.knowledgehub.resource.enums.ModaliteTravail;
import tn.esprim.knowledgehub.resource.enums.TypeAnnonceStage;
import tn.esprim.knowledgehub.resource.repository.AnnonceStageRepository;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class AnnonceService {

    private final AnnonceStageRepository annonceStageRepository;
    private final MinioClient minioClient;
    private final MinioClient minioClientForPresignedUrls;
    private final Tika tika = new Tika();

    @Value("${minio.buckets.annonces}")
    private String annoncesBucket;

    @Value("${minio.external-endpoint}")
    private String externalEndpoint;

    public AnnonceService(
            AnnonceStageRepository annonceStageRepository,
            MinioClient minioClient,
            @Qualifier("minioClientForPresignedUrls") MinioClient minioClientForPresignedUrls
    ) {
        this.annonceStageRepository = annonceStageRepository;
        this.minioClient = minioClient;
        this.minioClientForPresignedUrls = minioClientForPresignedUrls;
    }

    // ── lecture ──────────────────────────────────────────────────

    public AnnonceStage findById(long id) {
        return annonceStageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annonce introuvable : " + id));
    }

    public List<AnnonceStage> findAll() {
        return annonceStageRepository.findAll();
    }

    public List<AnnonceStage> findAllActive() {
        return annonceStageRepository.findByEstActiveTrue();
    }

    public List<AnnonceStage> findByType(TypeAnnonceStage type) {
        return annonceStageRepository.findByTypesStageContaining(type);
    }

    public List<AnnonceStage> findByModalite(ModaliteTravail modalite) {
        return annonceStageRepository.findByModaliteTravail(modalite);
    }

    public List<AnnonceStage> findByVille(String ville) {
        return annonceStageRepository.findByVilleIgnoreCase(ville);
    }

    public List<AnnonceStage> search(String keyword) {
        return annonceStageRepository
                .findByTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword);
    }

    // ── écriture ─────────────────────────────────────────────────

    @Transactional
    public AnnonceStage save(AnnonceStage annonce, MultipartFile image) {
        annonce.setEstActive(true);
        // Ensure bidirectional relation and validate each AnnonceUrl before persisting
        if (annonce.getUrls() != null) {
            annonce.getUrls().forEach(u -> {
                u.setAnnonce(annonce);
                if (u.getTypeUrl() == null) {
                    throw new IllegalArgumentException("Chaque URL doit avoir un type (typeUrl)");
                }
                if (u.getUrl() == null || u.getUrl().isBlank()) {
                    throw new IllegalArgumentException("Chaque URL doit avoir une url non vide");
                }
            });
        }
        if (image != null && !image.isEmpty()) {
            String imageUrl = uploadAnnonceImage(image);
            annonce.setImageUrl(imageUrl);
        }
        return annonceStageRepository.save(annonce);
    }

    @Transactional
    public AnnonceStage edit(long id, AnnonceStage updated, MultipartFile image) {
        AnnonceStage existing = findById(id);
        existing.setTitre(updated.getTitre());
        existing.setDescription(updated.getDescription());
        existing.setNomEntreprise(updated.getNomEntreprise());
        existing.setVille(updated.getVille());
        existing.setPays(updated.getPays());
        existing.setModaliteTravail(updated.getModaliteTravail());
        existing.setTypesStage(updated.getTypesStage());
        
        // Mutate the existing collection instead of replacing it to avoid Hibernate cascade issues
        // Clear existing URLs and add updated ones
        existing.getUrls().clear();
        if (updated.getUrls() != null) {
            for (AnnonceUrl u : updated.getUrls()) {
                u.setAnnonce(existing);
                if (u.getTypeUrl() == null) {
                    throw new IllegalArgumentException("Chaque URL doit avoir un type (typeUrl)");
                }
                if (u.getUrl() == null || u.getUrl().isBlank()) {
                    throw new IllegalArgumentException("Chaque URL doit avoir une url non vide");
                }
                existing.getUrls().add(u);
            }
        }

        if (image != null && !image.isEmpty()) {
            if (existing.getImageUrl() != null) {
                deleteAnnonceImage(existing.getImageUrl());
            }
            existing.setImageUrl(uploadAnnonceImage(image));
        }

        return annonceStageRepository.save(existing);
    }

    public AnnonceStage toggleActive(long id) {
        AnnonceStage annonce = findById(id);
        annonce.setEstActive(!annonce.isEstActive());
        return annonceStageRepository.save(annonce);
    }

    @Transactional
    public void deleteById(long id) {
        AnnonceStage annonce = findById(id);
        if (annonce.getImageUrl() != null) {
            deleteAnnonceImage(annonce.getImageUrl());
        }

        annonceStageRepository.deleteById(id);
    }

    // ── MinIO ─────────────────────────────────────────────────────

    private String uploadAnnonceImage(MultipartFile image) {
        try {
            String contentType = tika.detect(image.getInputStream());
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Le fichier doit être une image");
            }

            String extension = extensionFromContentType(contentType);
            String objectName = "annonce-" + UUID.randomUUID() + extension;

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(annoncesBucket)
                            .object(objectName)
                            .stream(image.getInputStream(), image.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );

            String directUrl = externalEndpoint + "/" + annoncesBucket + "/" + objectName;
            log.info("Image annonce uploadée, URL publique : {}", directUrl);
            return directUrl;

        } catch (Exception e) {
            log.error("Erreur upload image annonce : {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload annonce image: " + e.getMessage(), e);
        }
    }

    private void deleteAnnonceImage(String imageUrl) {
        try {
            String objectName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(annoncesBucket)
                            .object(objectName)
                            .build()
            );
            log.info("Image annonce supprimée : {}", objectName);
        } catch (Exception e) {
            log.warn("Impossible de supprimer l'image annonce : {}", e.getMessage());
        }
    }

    private String extensionFromContentType(String contentType) {
        return switch (contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif"  -> ".gif";
            case "image/bmp"  -> ".bmp";
            default           -> ".png";
        };
    }
}